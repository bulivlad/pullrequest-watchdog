package io.watchdog.pullrequest.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author vladclaudiubulimac on 2019-03-04.
 */

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RestResponse {

    String changedEntity;
    String entityName;

}
