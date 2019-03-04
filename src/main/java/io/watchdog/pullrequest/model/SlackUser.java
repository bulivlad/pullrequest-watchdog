package io.watchdog.pullrequest.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author vladclaudiubulimac on 2019-03-01.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlackUser extends User {

    String email;

}
