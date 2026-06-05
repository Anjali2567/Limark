package ai.leadplus.domain.common;

import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class StringListConverter extends TypedJsonConverter<List<String>> {
    public StringListConverter() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
    }
}
