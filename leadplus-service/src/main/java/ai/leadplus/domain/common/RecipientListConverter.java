package ai.leadplus.domain.common;

import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class RecipientListConverter extends TypedJsonConverter<List<Recipient>> {
    public RecipientListConverter() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, Recipient.class));
    }
}
