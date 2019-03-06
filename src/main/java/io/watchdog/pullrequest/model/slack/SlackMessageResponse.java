package io.watchdog.pullrequest.model.slack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author vladclaudiubulimac on 2019-03-06.
 */

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackMessageResponse {

    String ok;
    String channel;
    String ts;
    Message message;
    String error;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private class Message {
        String type;
        String subtype;
        String text;
        String ts;
        String username;
        @JsonProperty("bot_id")
        String botId;
    }

}
