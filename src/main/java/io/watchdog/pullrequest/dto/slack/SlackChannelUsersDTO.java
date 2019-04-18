package io.watchdog.pullrequest.dto.slack;

import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

/**
 * @author vladclaudiubulimac on 2019-03-29.
 */

@Data
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlackChannelUsersDTO {

    String ok;
    String error;
    Set<String> members;
    SlackChannelDTO.Channel channel;

}
