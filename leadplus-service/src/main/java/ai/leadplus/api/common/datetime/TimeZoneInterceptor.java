package ai.leadplus.api.common.datetime;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Slf4j
@Component
public class TimeZoneInterceptor implements HandlerInterceptor {
    private static final String TIMEZONE_HEADER = "X-TimeZone";

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String tz = request.getHeader(TIMEZONE_HEADER);
        if (tz == null || tz.isBlank()) {
            TimeZoneContext.setTimeZone("UTC");
        } else {
            TimeZoneContext.setTimeZone(tz);
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        TimeZoneContext.clear();
    }
}
