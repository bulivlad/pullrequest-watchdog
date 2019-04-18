package io.watchdog.pullrequest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vladclaudiubulimac on 2019-03-01.
 */

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginatedPullRequestDTO {

    @JsonProperty("values")
    List<PullRequestDTO> pullRequests = new ArrayList<>();

    @JsonProperty("pagelen")
    Long pageLength;

    Long size;

    Long page;

    String next;
}
