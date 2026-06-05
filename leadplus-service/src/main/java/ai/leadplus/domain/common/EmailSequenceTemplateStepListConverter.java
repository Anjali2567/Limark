package ai.leadplus.domain.common;

import ai.leadplus.domain.emailsequencetemplates.EmailSequenceTemplateStep;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class EmailSequenceTemplateStepListConverter extends TypedJsonConverter<List<EmailSequenceTemplateStep>> {
    public EmailSequenceTemplateStepListConverter() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, EmailSequenceTemplateStep.class));
    }
}
