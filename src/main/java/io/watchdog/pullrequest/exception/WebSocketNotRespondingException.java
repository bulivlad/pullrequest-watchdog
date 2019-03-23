package io.watchdog.pullrequest.exception;

/**
 * @author vladclaudiubulimac on 2019-03-23.
 */
public class WebSocketNotRespondingException extends RuntimeException {

    public WebSocketNotRespondingException() {
        super();
    }

    public WebSocketNotRespondingException(String message) {
        super(message);
    }

    public WebSocketNotRespondingException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebSocketNotRespondingException(Throwable cause) {
        super(cause);
    }
}
