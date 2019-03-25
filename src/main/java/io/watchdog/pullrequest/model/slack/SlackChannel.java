package io.watchdog.pullrequest.model.slack;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author vladclaudiubulimac on 2019-03-25.
 */

@Data
@Document
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlackChannel {

    @Id
    String id;
    @NotNull
    String name;
    List<String> previousNames;

}
