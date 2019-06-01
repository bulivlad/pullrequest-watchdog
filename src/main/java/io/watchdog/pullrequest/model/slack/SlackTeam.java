package io.watchdog.pullrequest.model.slack;

import io.watchdog.pullrequest.model.CorrelatedUser;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author vladclaudiubulimac on 2019-03-02.
 */

@Data
@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@CompoundIndex(name = "channel_name_slug_uniqe", unique = true, def = "{'name' : 1, 'channel' : 1, 'slug' : 1}")
public class SlackTeam {

    @Id
    String id;
    @Singular
    List<CorrelatedUser> members;
    @NonNull
    String channel;
    String checkingSchedule;
    @NotNull
    String name;
    @Builder.Default
    boolean tagMembers = true;
    String slug;

}
