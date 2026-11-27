package ca.allyscan.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SiteCrawlerTest {

    private HttpServer server;
    private String base;
    private final SiteCrawler crawler = new SiteCrawler(500);

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private void respond(String path, int status, String html) {
        server.createContext(path, exchange -> {
            byte[] body = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
    }

    @Test
    void refusedConnectionAtRootThrowsWithReason() throws IOException {
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            closedPort = socket.getLocalPort();
        }
        String root = "http://127.0.0.1:" + closedPort + "/";

        var e = assertThrows(SiteUnreachableException.class, () -> crawler.discover(root));

        assertEquals("Could not reach " + root + " because the server refused the connection.", e.getMessage());
    }

    @Test
    void non2xxAtRootThrowsWithStatusCode() {
        respond("/", 503, "down for maintenance");

        var e = assertThrows(SiteUnreachableException.class, () -> crawler.discover(base + "/"));

        assertEquals("Could not reach " + base + "/ because the server responded with HTTP 503 (server error).",
                e.getMessage());
    }

    @Test
    void slowRootThrowsTimeout() {
        server.createContext("/", exchange -> {
            try {
                Thread.sleep(3_000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            exchange.close();
        });

        var e = assertThrows(SiteUnreachableException.class, () -> crawler.discover(base + "/"));

        assertTrue(e.getMessage().endsWith("because the server did not respond within 500 ms."), e.getMessage());
    }

    @Test
    void unknownHostIsDescribedAsDnsFailure() {
        String reason = new SiteCrawler().describe(new UnknownHostException("dequeuniversity.com"));

        assertEquals("its domain name could not be found (DNS lookup for dequeuniversity.com failed)", reason);
    }

    @Test
    void proxyRefusalIsDescribedPlainly() {
        String reason = new SiteCrawler().describe(new IOException(
                "Unable to tunnel through proxy. Proxy returns \"HTTP/1.1 403 Forbidden\""));

        assertEquals("a network proxy blocked the connection (proxy returned HTTP 403)", reason);
    }

    @Test
    void invalidRootUrlThrows() {
        var e = assertThrows(SiteUnreachableException.class, () -> crawler.discover("not a url"));

        assertEquals("Could not reach not a url because it is not a valid http or https address.", e.getMessage());
    }

    @Test
    void brokenLinkBelowRootIsSkippedNotFatal() {
        respond("/", 200, "<a href='/ok'>ok</a> <a href='/missing'>missing</a> <a href='https://elsewhere.test/'>x</a>");
        respond("/ok", 200, "<p>fine</p>");
        respond("/missing", 404, "not found");

        List<String> pages = crawler.discover(base + "/");

        assertEquals(List.of(base + "/", base + "/ok"), pages);
    }
}
