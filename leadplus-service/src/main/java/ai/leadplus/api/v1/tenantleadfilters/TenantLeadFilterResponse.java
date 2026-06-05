package ai.leadplus.api.v1.tenantleadfilters;

import ai.leadplus.application.tenantleadfilters.TenantLeadFilterDto;
import ai.leadplus.domain.common.LeadType;
import ai.leadplus.domain.leadcompanies.EmployeeRange;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantLeadFilterResponse {

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

    private LocalDateTime createdAt;

    public static TenantLeadFilterResponse fromDto(TenantLeadFilterDto dto) {
        return TenantLeadFilterResponse.builder()
                .id(dto.getId())
                .tenantId(dto.getTenantId())
                .type(dto.getType())
                .cities(dto.getCities())
                .states(dto.getStates())
                .countries(dto.getCountries())
                .companyNames(dto.getCompanyNames())
                .companyCities(dto.getCompanyCities())
                .companyStates(dto.getCompanyStates())
                .companyCountries(dto.getCompanyCountries())
                .regions(dto.getRegions())
                .keywords(dto.getKeywords())
                .industries(dto.getIndustries())
                .employeeRanges(dto.getEmployeeRanges())
                .revenueRanges(dto.getRevenueRanges())
                .technologies(dto.getTechnologies())
                .titles(dto.getTitles())
                .seniority(dto.getSeniority())
                .departments(dto.getDepartments())
                .postalCodes(dto.getPostalCodes())
                .sicCodes(dto.getSicCodes())
                .naicsCodes(dto.getNaicsCodes())
                .active(dto.isActive())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
