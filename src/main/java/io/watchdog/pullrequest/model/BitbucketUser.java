package io.watchdog.pullrequest.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author vladclaudiubulimac on 2019-03-01.
 */

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BitbucketUser extends User {

    Role role;
    Boolean approved;

    @Builder
    public BitbucketUser(String username, String name, Role role, Boolean approved) {
        super(username, name);
        this.role = role;
        this.approved = approved;
    }

}
