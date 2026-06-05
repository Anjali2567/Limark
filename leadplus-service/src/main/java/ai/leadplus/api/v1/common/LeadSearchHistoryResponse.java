package ai.leadplus.api.v1.common;

import ai.leadplus.application.leadsearchhistories.LeadSearchHistoryDto;
import ai.leadplus.domain.leadcompanies.EmployeeRange;
import ai.leadplus.domain.common.LeadType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadSearchHistoryResponse {
    private Long id;
    private Long userId;
    private String title;
    private Integer resultCount;
    private LeadType type;
    private List<String> cities;
    private List<String> states;
    private List<String> countries;
    private List<String> contactNames;
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
    private LocalDateTime createdAt;

    public static LeadSearchHistoryResponse toResponse(LeadSearchHistoryDto dto) {
        if (dto == null) {
            return null;
        }
        return LeadSearchHistoryResponse.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .title(dto.getTitle())
                .resultCount(dto.getResultCount())
                .type(dto.getType())
                .cities(dto.getCities())
                .states(dto.getStates())
                .countries(dto.getCountries())
                .contactNames(dto.getContactNames())
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
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
