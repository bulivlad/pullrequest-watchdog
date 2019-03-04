package io.watchdog.pullrequest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.watchdog.pullrequest.model.State;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author vladclaudiubulimac on 2019-03-01.
 */

@Slf4j
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequestDTO {

    Long id;

    String title;

    String description;

    @JsonProperty("close_source_branch")
    Boolean closeSourceBranch;

    String repository;

    String sourceBranch;

    String destinationBranch;

    @JsonProperty("created_on")
    ZonedDateTime createdOn;

    State state;

    List<ParticipantDTO> participants;

    List<ReviewerDTO> reviewers;

    ReviewerDTO author;

    @JsonProperty("source")
    public void setNestedSource(Map<String, Object> source) {
        this.repository = ((Map<String, String>) source.get("repository")).get("full_name");
        this.sourceBranch = ((Map<String, String>) source.get("branch")).get("name");
    }

    @JsonProperty("destination")
    public void setNestedDestination(Map<String, Object> destination) {
        this.destinationBranch = ((Map<String, String>) destination.get("branch")).get("name");
    }

}
