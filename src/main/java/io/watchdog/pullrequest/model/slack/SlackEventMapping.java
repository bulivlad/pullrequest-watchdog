package io.watchdog.pullrequest.model.slack;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * @author vladclaudiubulimac on 2019-03-15.
 */

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum SlackEventMapping {

    DEFAULT (""),
    ADD_TEAM_EVENT_REGEX("(?i)(add team\\s)(?<teamName>[A-Za-z0-9\\-\\_\\.]+).*(members\\s\\[)(?<members>.*)(\\]).*(and\\sscheduler\\s)(?<schedulerExpression>.*)"),
    REMOVE_TEAM_EVENT_REGEX("(?i)(remove team\\s)(?<teamName>.*)"),
    UNSCHEDULE_TEAM_EVENT_REGEX("(?i)(unschedule team\\s)(?<teamName>.*)");

    @Getter
    String value;

}
