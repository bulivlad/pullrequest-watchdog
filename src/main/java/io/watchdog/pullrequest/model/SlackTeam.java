package io.watchdog.pullrequest.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
@CompoundIndex(name = "channel_name_uniqe", unique = true, def = "{'name' : 1, 'channel' : 1}")
public class SlackTeam {

    @Id
    String id;
    List<SlackUser> members;
    @NonNull
    String channel;
    String checkingSchedule;
    @NotNull
    String name;
    boolean tagMembers = true;

}
