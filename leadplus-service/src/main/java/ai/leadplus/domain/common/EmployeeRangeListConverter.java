package ai.leadplus.domain.common;

import ai.leadplus.domain.leadcompanies.EmployeeRange;
import jakarta.persistence.Converter;

@Converter
public class EmployeeRangeListConverter extends EnumListConverter<EmployeeRange> {

    public EmployeeRangeListConverter() {
        super(EmployeeRange.class);
    }
}
