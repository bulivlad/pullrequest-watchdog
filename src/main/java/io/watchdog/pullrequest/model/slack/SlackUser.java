package io.watchdog.pullrequest.model.slack;

import io.watchdog.pullrequest.model.User;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author vladclaudiubulimac on 2019-03-01.
 */

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlackUser extends User {

    String email;
    String mention;

    @Builder
    public SlackUser(String username, String name, String email, String mention) {
        super(username, name);
        this.email = email;
        this.mention = mention;
    }
}
