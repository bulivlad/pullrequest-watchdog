package io.watchdog.pullrequest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author vladclaudiubulimac on 2019-03-07.
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BitbucketUserDTO {

    String username;
    @JsonProperty("last_name")
    String lastName;
    @JsonProperty("first_name")
    String firstName;
    @JsonProperty("display_name")
    String displayName;

}
