package io.watchdog.pullrequest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author vladclaudiubulimac on 2019-03-07.
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BitbucketUserDTOWrapper {

    BitbucketUserDTO user;

}
