package ai.leadplus.api.common.datetime;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class LocalDateTimeResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, @NonNull MethodParameter returnType, @NonNull MediaType selectedContentType,
                                  @NonNull Class selectedConverterType, @NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {

        if (body == null) {
            return null;
        }

        if (selectedContentType != null && !selectedContentType.includes(MediaType.APPLICATION_JSON)) {
            return body;
        }

        var clientZone = TimeZoneContext.getTimeZone();
        log.debug("Timezone in use: {}", clientZone);

        try {
            if (body instanceof LocalDateTime utcTime) {
                return utcTime.atZone(ZoneOffset.UTC)
                        .withZoneSameInstant(clientZone)
                        .toLocalDateTime();
            }

            // Manually handle Spring PageImpl content
            if (body instanceof org.springframework.data.domain.Page<?> page) {
                log.debug("Handling page content size: {}", page.getContent().size());
                for (Object item : page.getContent()) {
                    safeConvertLocalDateTimes(item, clientZone);
                }
                return body;
            }

            safeConvertLocalDateTimes(body, clientZone);

        } catch (UnsupportedOperationException e) {
            log.warn("Cannot modify response object (likely immutable): {}. Skipping timezone conversion.",
                    body.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("Error during timezone conversion: {}", e.getMessage(), e);
        }

        return body;
    }

    private void safeConvertLocalDateTimes(Object obj, java.time.ZoneId clientZone) {
        if (obj == null) {
            return;
        }

        if (obj instanceof Collection<?> collection) {
            for (Object item : collection) {
                safeConvertLocalDateTimes(item, clientZone);
            }
            return;
        }

        if (obj instanceof Map<?, ?> map) {
            for (Object value : map.values()) {
                safeConvertLocalDateTimes(value, clientZone);
            }
            return;
        }

        if (obj.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                Object arrayElement = java.lang.reflect.Array.get(obj, i);
                safeConvertLocalDateTimes(arrayElement, clientZone);
            }
            return;
        }

        Class<?> clazz = obj.getClass();
        if (clazz.isPrimitive() || clazz.isEnum()) {
            return;
        }

        // Skip java.* types (JDK classes) except Page
        if ((clazz.getName().startsWith("java.") || clazz.getName().startsWith("javax."))
                && !(obj instanceof org.springframework.data.domain.Page)) {
            return;
        }

        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            Field[] fields = current.getDeclaredFields();
            for (Field field : fields) {
                try {
                    if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }

                    field.setAccessible(true);
                    Object value = field.get(obj);

                    if (value == null) {
                        continue;
                    }

                    if (value instanceof LocalDateTime utcTime) {
                        var converted = utcTime.atZone(ZoneOffset.UTC)
                                .withZoneSameInstant(clientZone)
                                .toLocalDateTime();
                        field.set(obj, converted);
                    } else {
                        safeConvertLocalDateTimes(value, clientZone);
                    }

                } catch (Exception e) {
                    log.debug("Error processing field '{}' in class '{}': {}",
                            field.getName(), clazz.getSimpleName(), e.getMessage());
                }
            }
        }
    }
}
