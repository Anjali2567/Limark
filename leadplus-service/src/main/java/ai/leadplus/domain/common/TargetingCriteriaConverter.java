package ai.leadplus.domain.common;

import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.persistence.Converter;

@Converter
public class TargetingCriteriaConverter extends TypedJsonConverter<TargetingCriteria> {
    public TargetingCriteriaConverter() {
        super(TypeFactory.defaultInstance().constructType(TargetingCriteria.class));
    }
}
