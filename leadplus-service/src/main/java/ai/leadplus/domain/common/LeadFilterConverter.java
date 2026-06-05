package ai.leadplus.domain.common;

import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.persistence.Converter;

@Converter
public class LeadFilterConverter extends TypedJsonConverter<LeadFilter> {
    public LeadFilterConverter() {
        super(TypeFactory.defaultInstance().constructType(LeadFilter.class));
    }
}
