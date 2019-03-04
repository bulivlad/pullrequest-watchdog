package io.watchdog.pullrequest.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Method;
import java.util.regex.Matcher;

/**
 * @author vladclaudiubulimac on 06/03/2018.
 */
@Data
@Builder
@EqualsAndHashCode
public class MethodWrapper {

    private Method method;
    private String pattern;
    private Matcher matcher;
    private String next;

}
