package ca.allyscan.audit;

/**
 * Thrown when the root URL of a scan cannot be fetched at all, so there is nothing to crawl.
 * The message is shown to the user on the report page, so it is written as a plain sentence.
 */
public class SiteUnreachableException extends RuntimeException {

    public SiteUnreachableException(String rootUrl, String reason, Throwable cause) {
        super("Could not reach " + rootUrl + " because " + reason + ".", cause);
    }
}
