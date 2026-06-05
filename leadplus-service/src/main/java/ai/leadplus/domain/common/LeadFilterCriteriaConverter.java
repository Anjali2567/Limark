package ai.leadplus.domain.common;

import ai.leadplus.application.leads.LeadFilterCriteria;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.persistence.Converter;

@Converter
public class LeadFilterCriteriaConverter extends TypedJsonConverter<LeadFilterCriteria> {
    public LeadFilterCriteriaConverter() {
        super(TypeFactory.defaultInstance().constructType(LeadFilterCriteria.class));
    }
}
