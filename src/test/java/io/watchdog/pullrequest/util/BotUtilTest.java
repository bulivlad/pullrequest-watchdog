package io.watchdog.pullrequest.util;

import io.watchdog.pullrequest.model.slack.SlackEventMapping;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static io.watchdog.pullrequest.model.slack.SlackEventMapping.ADD_TEAM_EVENT_REGEX;
import static io.watchdog.pullrequest.model.slack.SlackEventMapping.REMOVE_TEAM_EVENT_REGEX;
import static io.watchdog.pullrequest.model.slack.SlackEventMapping.UNSCHEDULE_TEAM_EVENT_REGEX;
import static io.watchdog.pullrequest.util.BotUtil.getGroupMatcherFromEventMessage;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author vladclaudiubulimac on 22/08/2018.
 */

@RunWith(MockitoJUnitRunner.class)
public class BotUtilTest {

    @Test
    public void getGroupMatcherForAddTeamEvent() {
        String message = "@bot AdD team dummy-team WIth FOLLowing members [@member1, @member2] AND scHEDulEr 0 20 11 1/1 * ? *";

        Optional<String> teamName = getGroupMatcherFromEventMessage(message, ADD_TEAM_EVENT_REGEX.getValue(), "teamName");

        assertThat(teamName, is(notNullValue()));
        assertThat(teamName.isPresent(), is(true));
        assertThat(teamName.get(), is(equalTo("dummy-team")));

        Optional<String> members = getGroupMatcherFromEventMessage(message, ADD_TEAM_EVENT_REGEX.getValue(), "members");

        assertThat(members, is(notNullValue()));
        assertThat(members.isPresent(), is(true));
        assertThat(members.get(), is(equalTo("@member1, @member2")));

        Optional<String> schedulerExpression = getGroupMatcherFromEventMessage(message, ADD_TEAM_EVENT_REGEX.getValue(), "schedulerExpression");

        assertThat(schedulerExpression, is(notNullValue()));
        assertThat(schedulerExpression.isPresent(), is(true));
        assertThat(schedulerExpression.get(), is(equalTo("0 20 11 1/1 * ? *")));
    }

    @Test
    public void getGroupMatcherForRemoveTeamEvent() {
        String message = "@bot REMOve team dummy-team";

        Optional<String> teamName = getGroupMatcherFromEventMessage(message, REMOVE_TEAM_EVENT_REGEX.getValue(), "teamName");

        assertThat(teamName, is(notNullValue()));
        assertThat(teamName.isPresent(), is(true));
        assertThat(teamName.get(), is(equalTo("dummy-team")));
    }

    @Test
    public void getGroupMatcherForUnscheduleTeamEvent() {
        String message = "@bot UNSChedule team dummy-team";

        Optional<String> teamName = getGroupMatcherFromEventMessage(message, UNSCHEDULE_TEAM_EVENT_REGEX.getValue(), "teamName");

        assertThat(teamName, is(notNullValue()));
        assertThat(teamName.isPresent(), is(true));
        assertThat(teamName.get(), is(equalTo("dummy-team")));
    }

    @Test
    public void getGroupMatcherForUnknownEvent() {
        String message = "@bot UNSChedule my team dummy-team";

        Optional<String> teamName = getGroupMatcherFromEventMessage(message, SlackEventMapping.HEALTH_CHECK_MESSAGE_REGEX.getValue(), "teamName");

        assertThat(teamName, is(notNullValue()));
        assertThat(teamName.isPresent(), is(false));
    }
}