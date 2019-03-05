package io.watchdog.pullrequest.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author vladclaudiubulimac on 2019-03-01.
 */

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlackUser extends User {

    String email;

    @Builder
    public SlackUser(String username, String name, String email) {
        super(username, name);
        this.email = email;
    }
}
