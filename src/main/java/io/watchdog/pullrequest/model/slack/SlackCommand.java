package io.watchdog.pullrequest.model.slack;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * @author vladclaudiubulimac on 2019-03-06.
 */

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum SlackCommand {

    MESSAGE_CHANNEL("chat.postMessage");

    @Getter
    String value;

}
