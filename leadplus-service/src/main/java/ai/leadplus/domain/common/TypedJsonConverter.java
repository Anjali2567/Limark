package ai.leadplus.domain.common;

import jakarta.persistence.AttributeConverter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.format.DateTimeFormatter;

@Slf4j
public abstract class TypedJsonConverter<T> implements AttributeConverter<T, String> {

    // Accepts both "2026-04-17T06:54:39.424" (ISO) and "2026-04-17 06:54:39.424000"
    private static final DateTimeFormatter FLEXIBLE_DATETIME_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("yyyy-MM-dd")
            .optionalStart().appendLiteral('T').optionalEnd()
            .optionalStart().appendLiteral(' ').optionalEnd()
            .appendPattern("HH:mm:ss")
            .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true).optionalEnd()
            .toFormatter();

    // Handles legacy datetime strings stored for LocalTime fields (e.g. "2026-04-06 08:00:00")
    // by parsing as LocalDateTime and extracting the time component.
    private static class FlexibleLocalTimeDeserializer extends StdDeserializer<LocalTime> {
        FlexibleLocalTimeDeserializer() { super(LocalTime.class); }

        @Override
        public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getText().trim();
            // "HH:mm:ss" is 8 chars — anything longer is a datetime string
            if (value.length() > 8) {
                return LocalDateTime.parse(value, FLEXIBLE_DATETIME_FORMATTER).toLocalTime();
            }
            return LocalTime.parse(value);
        }
    }

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()
                    .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(FLEXIBLE_DATETIME_FORMATTER))
                    .addDeserializer(LocalTime.class, new FlexibleLocalTimeDeserializer()))
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final JavaType javaType;

    protected TypedJsonConverter(JavaType javaType) {
        this.javaType = javaType;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            log.error("Error converting object to JSON: {}", attribute, e);
            throw new RuntimeException("Could not convert object to JSON", e);
        }
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, javaType);
        } catch (Exception e) {
            log.error("Error converting JSON to object: {}", dbData, e);
            throw new RuntimeException("Could not convert JSON to object", e);
        }
    }
}
