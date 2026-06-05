package ai.leadplus.domain.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Generic JSON converter for persisting complex objects as JSON in the database.
 * 
 * NOTE: This converter deserializes to Object.class, which may result in:
 * - Enums deserializing as strings
 * - Nested objects deserializing as LinkedHashMap
 * - Potential type loss on complex objects
 * 
 * For entities with complex type requirements, consider using:
 * - Type-specific converters with proper target classes
 * - @Convert(converter = CustomConverter.class) with explicit type handling
 * - Dedicated DTO classes with @Convert support
 */
@Slf4j
@Converter
public class JsonConverter implements AttributeConverter<Object, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Object attribute) {
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
    public Object convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, Object.class);
        } catch (Exception e) {
            log.error("Error converting JSON to object: {}", dbData, e);
            throw new RuntimeException("Could not convert JSON to object", e);
        }
    }
}
