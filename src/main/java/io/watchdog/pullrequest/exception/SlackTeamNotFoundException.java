package io.watchdog.pullrequest.exception;

/**
 * @author vladclaudiubulimac on 2019-03-23.
 */
public class SlackTeamNotFoundException extends Exception {

    public SlackTeamNotFoundException() {
        super();
    }

    public SlackTeamNotFoundException(String message) {
        super(message);
    }

    public SlackTeamNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SlackTeamNotFoundException(Throwable cause) {
        super(cause);
    }
}
