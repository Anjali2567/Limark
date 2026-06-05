package ai.leadplus.application.useractivitylogs;

import ai.leadplus.domain.useractivitylogs.LogLevel;
import ai.leadplus.domain.useractivitylogs.UserActivityLogType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TrackUserActivity {

    String message();

    LogLevel level() default LogLevel.INFO;

    UserActivityLogType type();
}