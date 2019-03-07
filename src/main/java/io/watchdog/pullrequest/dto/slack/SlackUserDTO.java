package io.watchdog.pullrequest.dto.slack;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Map;

/**
 * @author vladclaudiubulimac on 2019-03-07.
 */

@Data
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlackUserDTO {

    String ok;
    String error;
    User user;

    @Data
    @ToString
    private class User {
        String id;
        String email;
        String displayName;
        String realName;

        @JsonProperty("profile")
        private void setProfileInfo(Map<String, String> profile){
            this.email = profile.get("email");
            this.displayName = profile.get("display_name");
            this.realName = profile.get("real_name");
        }
    }

    public String getUserEmail() {
        return user.getEmail();
    }

    public String getUserDisplayName() {
        return user.getDisplayName();
    }

    public String getUserRealName() {
        return user.getRealName();
    }

}
