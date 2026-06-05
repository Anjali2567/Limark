package ai.leadplus.api.common.datetime;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeZoneContext {
    private static final ThreadLocal<ZoneId> context = ThreadLocal.withInitial(() -> ZoneId.of("UTC"));

    public static ZoneId getTimeZone() {
        return context.get();
    }

    public static void setTimeZone(String timeZoneId) {
        try {
            ZoneId zone = ZoneId.of(timeZoneId);
            context.set(zone);
        } catch (Exception e) {
            log.warn("Invalid timezone '{}' received. Falling back to UTC.", timeZoneId);
            context.set(ZoneId.of("UTC"));
        }
    }

    public static void clear() {
        context.remove();
    }
}
