package ai.leadplus.domain.common;

import ai.leadplus.domain.vendors.Answer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class AnswerListConverter extends TypedJsonConverter<List<Answer>> {
    public AnswerListConverter() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, Answer.class));
    }
}
