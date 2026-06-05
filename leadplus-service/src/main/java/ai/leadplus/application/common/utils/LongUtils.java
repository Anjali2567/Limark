package ai.leadplus.application.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class LongUtils {

    public static Optional<Long> tryParseLong(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(value.trim()));
        } catch (NumberFormatException e) {
            log.debug("Failed to parse '{}' as Long", value);
            return Optional.empty();
        }
    }

    public static Long parseLongOrDefault(String value, Long defaultValue) {
        return tryParseLong(value).orElse(defaultValue);
    }
}
