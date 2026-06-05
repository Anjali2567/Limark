package ai.leadplus.application.tenantleadfilters;

import ai.leadplus.domain.common.LeadType;
import ai.leadplus.domain.leadcompanies.EmployeeRange;
import ai.leadplus.domain.tenantleadfilters.TenantLeadFilter;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantLeadFilterDto {

    private Long id;
    private Long tenantId;
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
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static TenantLeadFilterDto fromEntity(TenantLeadFilter entity) {
        return TenantLeadFilterDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .type(entity.getType())
                .cities(entity.getCities())
                .states(entity.getStates())
                .countries(entity.getCountries())
                .companyNames(entity.getCompanyNames())
                .companyCities(entity.getCompanyCities())
                .companyStates(entity.getCompanyStates())
                .companyCountries(entity.getCompanyCountries())
                .regions(entity.getRegions())
                .keywords(entity.getKeywords())
                .industries(entity.getIndustries())
                .employeeRanges(entity.getEmployeeRanges())
                .revenueRanges(entity.getRevenueRanges())
                .technologies(entity.getTechnologies())
                .titles(entity.getTitles())
                .seniority(entity.getSeniority())
                .departments(entity.getDepartments())
                .postalCodes(entity.getPostalCodes())
                .sicCodes(entity.getSicCodes())
                .naicsCodes(entity.getNaicsCodes())
                .active(entity.isActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TenantLeadFilter toEntity() {
        return TenantLeadFilter.builder()
                .id(id)
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
                .active(active)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
