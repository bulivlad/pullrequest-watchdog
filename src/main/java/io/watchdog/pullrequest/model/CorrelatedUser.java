package io.watchdog.pullrequest.model;

import io.watchdog.pullrequest.model.slack.SlackUser;
import lombok.Data;

/**
 * @author vladclaudiubulimac on 2019-03-07.
 */

@Data
public class CorrelatedUser {

    SlackUser slackUser;
    BitbucketUser bitbucketUser;

    public CorrelatedUser() {
    }

    public CorrelatedUser(SlackUser slackUser) {
        this.slackUser = slackUser;
    }
}
