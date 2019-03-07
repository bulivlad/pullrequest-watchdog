package io.watchdog.pullrequest.dto.slack;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.watchdog.pullrequest.model.slack.MessageType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author vladclaudiubulimac on 2019-03-06.
 */

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlackMessageRequestDTO {

    String channel;
    String text;
    @Singular
    List<Block> blocks;
    String threadTs;
    Boolean markdown = true;
    @JsonProperty("icon_emoji")
    String iconEmoji;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Block {
        String type = MessageType.SECTION.name().toLowerCase();
        Text text;

        public Block (String type) {
            this.type = type;
        }

        public Block (Text text){
            this.text = text;
        }
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Text {
        String type = MessageType.MRKDWN.name().toLowerCase();
        String text;

        public Text(String text){
            this.text = text;
        }
    }

}
