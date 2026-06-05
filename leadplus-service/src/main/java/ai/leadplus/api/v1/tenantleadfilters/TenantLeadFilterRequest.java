package ai.leadplus.api.v1.tenantleadfilters;

import ai.leadplus.application.tenantleadfilters.TenantLeadFilterDto;
import ai.leadplus.domain.common.LeadType;
import ai.leadplus.domain.leadcompanies.EmployeeRange;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantLeadFilterRequest {
    private LeadType type;
    private List<String> cities;
    private List<String> states;
    private List<String> countries;
    private List<String> companyNames;
    private List<String> companyCities;
    private List<String> companyStates;
    private List<String> companyCountries;
    private List<String> regions;
    private List<String> keywords;
    private List<String> industries;
    private List<EmployeeRange> employeeRanges;
    private List<String> revenueRanges;
    private List<String> technologies;
    private List<String> titles;
    private List<String> seniority;
    private List<String> departments;
    private List<String> postalCodes;
    private List<String> sicCodes;
    private List<String> naicsCodes;

    public TenantLeadFilterDto toDto(Long tenantId) {
        return TenantLeadFilterDto.builder()
                .tenantId(tenantId)
                .type(type)
                .cities(cities)
                .states(states)
                .countries(countries)
                .companyNames(companyNames)
                .companyCities(companyCities)
                .companyStates(companyStates)
                .companyCountries(companyCountries)
                .regions(regions)
                .keywords(keywords)
                .industries(industries)
                .employeeRanges(employeeRanges)
                .revenueRanges(revenueRanges)
                .technologies(technologies)
                .titles(titles)
                .seniority(seniority)
                .departments(departments)
                .postalCodes(postalCodes)
                .sicCodes(sicCodes)
                .naicsCodes(naicsCodes)
                .build();
    }
}