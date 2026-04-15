package fr.wdes.download;

/**
 * Thrown by a {@link Downloadable} when the server returned a non-2xx /
 * non-304 HTTP status. {@link #isPermanent()} distinguishes 4xx (the
 * server has definitively told us "no" - no point retrying) from 5xx
 * (probably transient, worth another shot).
 */
public class HttpStatusException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public final int status;

    public HttpStatusException(final int status) {
        super("Status du serveur : " + status);
        this.status = status;
    }

    /** 4xx - client / resource error, retries won't change the answer. */
    public boolean isPermanent() {
        return status >= 400 && status < 500;
    }
}
