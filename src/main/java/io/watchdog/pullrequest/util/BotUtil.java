package io.watchdog.pullrequest.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author vladclaudiubulimac on 2019-03-15.
 */
public class BotUtil {

    public static Optional<String> getGroupMatcherFromEventMessage(String message, String pattern, String groupName) {
        Matcher matcher = Pattern.compile(pattern).matcher(message);
        if(matcher.find()) {
            return Optional.of(matcher.group(groupName).trim());
        }
        return Optional.empty();
    }

}
