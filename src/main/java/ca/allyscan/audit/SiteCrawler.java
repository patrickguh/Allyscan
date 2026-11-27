package ca.allyscan.audit;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SiteCrawler {

    private static final int MAX_PAGES = 20;
    private static final int DEFAULT_TIMEOUT_MILLIS = 10_000;
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/120.0.0.0 Safari/537.36";

    /** Java's wording when an HTTPS proxy rejects the CONNECT request, e.g. a firewall blocking the host. */
    private static final Pattern PROXY_REFUSAL =
            Pattern.compile("Unable to tunnel through proxy\\. Proxy returns \"HTTP/[\\d.]+ (\\d{3})");

    private final int timeoutMillis;

    public SiteCrawler() {
        this(DEFAULT_TIMEOUT_MILLIS);
    }

    /** Lets tests use a short timeout. */
    SiteCrawler(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * Crawls same-host links starting at {@code rootUrl}.
     *
     * @throws SiteUnreachableException if the root URL itself cannot be fetched. Failures on
     *         other pages are skipped, since the rest of the site can still be scanned.
     */
    public List<String> discover(String rootUrl) {
        String root = stripFragment(rootUrl);
        String host = hostOf(root);

        Set<String> visited = new LinkedHashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(root);

        while (!queue.isEmpty() && visited.size() < MAX_PAGES) {
            String url = queue.poll();
            if (visited.contains(url)) {
                continue;
            }

            Document doc;
            try {
                doc = Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .timeout(timeoutMillis)
                        .get();
            } catch (IOException | IllegalArgumentException e) {
                if (url.equals(root)) {
                    throw new SiteUnreachableException(root, describe(e), e);
                }
                continue;
            }
            visited.add(url);

            for (var link : doc.select("a[href]")) {
                String absHref = stripFragment(link.absUrl("href"));
                if (absHref.isBlank() || visited.contains(absHref)) {
                    continue;
                }
                URI linkUri;
                try {
                    linkUri = URI.create(absHref);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                if (host.equals(linkUri.getHost())) {
                    queue.add(absHref);
                }
            }
        }

        return new ArrayList<>(visited);
    }

    private static String hostOf(String rootUrl) {
        String host = null;
        try {
            host = URI.create(rootUrl).getHost();
        } catch (IllegalArgumentException e) {
            // handled below
        }
        if (host == null) {
            throw new SiteUnreachableException(rootUrl, "it is not a valid http or https address", null);
        }
        return host;
    }

    /** Turns a fetch failure into the end of the sentence "Could not reach X because ...". */
    String describe(Exception e) {
        if (e instanceof UnknownHostException) {
            return "its domain name could not be found (DNS lookup for " + e.getMessage() + " failed)";
        }
        if (e instanceof SocketTimeoutException) {
            String limit = timeoutMillis % 1000 == 0 ? (timeoutMillis / 1000) + " seconds" : timeoutMillis + " ms";
            return "the server did not respond within " + limit;
        }
        if (e instanceof ConnectException) {
            return "the server refused the connection";
        }
        if (e instanceof NoRouteToHostException) {
            return "there is no network route to the server";
        }
        if (e instanceof SSLException) {
            return "a secure HTTPS connection could not be established";
        }
        if (e instanceof HttpStatusException status) {
            return "the server responded with HTTP " + status.getStatusCode() + statusHint(status.getStatusCode());
        }
        if (e instanceof UnsupportedMimeTypeException mime) {
            return "it returned " + mime.getMimeType() + " instead of an HTML page";
        }
        if (e instanceof IllegalArgumentException) {
            return "it is not a valid http or https address";
        }
        String detail = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        Matcher proxy = PROXY_REFUSAL.matcher(detail);
        if (proxy.find()) {
            return "a network proxy blocked the connection (proxy returned HTTP " + proxy.group(1) + ")";
        }
        return "of a network error (" + detail + ")";
    }

    private static String statusHint(int code) {
        return switch (code) {
            case 401, 403 -> " (access denied; the site may block automated scanners)";
            case 404 -> " (page not found)";
            case 429 -> " (too many requests)";
            default -> code >= 500 ? " (server error)" : "";
        };
    }

    private static String stripFragment(String url) {
        int hashIndex = url.indexOf('#');
        return hashIndex < 0 ? url : url.substring(0, hashIndex);
    }
}
