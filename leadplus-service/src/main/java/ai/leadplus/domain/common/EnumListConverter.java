package ai.leadplus.domain.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public abstract class EnumListConverter<E extends Enum<E>> implements AttributeConverter<List<E>, String> {

    private final Class<E> enumClass;

    protected EnumListConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String convertToDatabaseColumn(List<E> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "{}";
        }
        return "{" + attribute.stream()
                .map(Enum::name)
                .collect(Collectors.joining(",")) + "}";
    }

    @Override
    public List<E> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank() || dbData.equals("{}")) {
            return Collections.emptyList();
        }
        return Arrays.stream(dbData.replaceAll("^\\{|\\}$", "").split(","))
                .map(s -> Enum.valueOf(enumClass, s.trim().replace("\"", "")))
                .collect(Collectors.toList());
    }
}
