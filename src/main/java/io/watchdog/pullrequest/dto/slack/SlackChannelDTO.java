package io.watchdog.pullrequest.dto.slack;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author vladclaudiubulimac on 2019-03-25.
 */

@Data
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlackChannelDTO {

    String ok;
    String error;
    Channel channel;

    @Data
    @ToString
    public class Channel {
        String id;
        String name;
        @JsonProperty("is_archived")
        Boolean isArchived;
        @JsonProperty("previous_names")
        List<String> previousNames;

    }

    public String getChannelId() {
        return this.channel.getId();
    }

    public String getChannelName() {
        return this.channel.getName();
    }

    public boolean isChannelArchived() {
        return this.channel.getIsArchived();
    }

    public List<String> getChannelPreviousNames() {
        return this.channel.getPreviousNames();
    }

}
