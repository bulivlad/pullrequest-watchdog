package io.watchdog.pullrequest.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

/**
 * @author vladclaudiubulimac on 2019-03-01.
 */

@Data
@Builder
@Document
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PullRequest {

    String title;
    BitbucketUser author;
    List<BitbucketUser> reviewers;
    String sourceBranch;
    String targetBranch;
    LocalDate createdOn;
    Boolean approved;
    State state;

}
