package io.watchdog.pullrequest.dto.slack;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author vladclaudiubulimac on 2019-03-05.
 */

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlackTeamDTO {

    String teamName;
    String[] members;
    String scheduler;
    String channel;
    Boolean tagMembers = true;

}
