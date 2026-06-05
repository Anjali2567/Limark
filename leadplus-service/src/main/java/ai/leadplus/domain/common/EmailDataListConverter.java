package ai.leadplus.domain.common;

import ai.leadplus.domain.campaigncontacts.EmailData;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class EmailDataListConverter extends TypedJsonConverter<List<EmailData>> {
    public EmailDataListConverter() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, EmailData.class));
    }
}
