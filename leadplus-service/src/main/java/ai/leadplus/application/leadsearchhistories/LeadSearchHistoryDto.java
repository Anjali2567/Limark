package ai.leadplus.application.leadsearchhistories;

import ai.leadplus.domain.leadcompanies.EmployeeRange;
import ai.leadplus.domain.leadsearchhistories.LeadSearchHistory;
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
public class LeadSearchHistoryDto {
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
    private LocalDateTime updatedAt;

    public static LeadSearchHistoryDto fromEntity(LeadSearchHistory leadSearchHistory) {
        return LeadSearchHistoryDto.builder()
                .id(leadSearchHistory.getId())
                .userId(leadSearchHistory.getUserId())
                .title(leadSearchHistory.getTitle())
                .resultCount(leadSearchHistory.getResultCount())
                .type(leadSearchHistory.getType())
                .cities(leadSearchHistory.getCities())
                .states(leadSearchHistory.getStates())
                .countries(leadSearchHistory.getCountries())
                .contactNames(leadSearchHistory.getContactNames())
                .companyNames(leadSearchHistory.getCompanyNames())
                .companyCities(leadSearchHistory.getCompanyCities())
                .companyStates(leadSearchHistory.getCompanyStates())
                .companyCountries(leadSearchHistory.getCompanyCountries())
                .regions(leadSearchHistory.getRegions())
                .keywords(leadSearchHistory.getKeywords())
                .industries(leadSearchHistory.getIndustries())
                .employeeRanges(leadSearchHistory.getEmployeeRanges())
                .revenueRanges(leadSearchHistory.getRevenueRanges())
                .technologies(leadSearchHistory.getTechnologies())
                .titles(leadSearchHistory.getTitles())
                .seniority(leadSearchHistory.getSeniority())
                .departments(leadSearchHistory.getDepartments())
                .postalCodes(leadSearchHistory.getPostalCodes())
                .sicCodes(leadSearchHistory.getSicCodes())
                .naicsCodes(leadSearchHistory.getNaicsCodes())
                .createdAt(leadSearchHistory.getCreatedAt())
                .updatedAt(leadSearchHistory.getUpdatedAt())
                .build();
    }

    public LeadSearchHistory toEntity() {
        return LeadSearchHistory.builder()
                .id(id)
                .userId(userId)
                .title(title)
                .resultCount(resultCount)
                .type(type)
                .cities(cities)
                .states(states)
                .countries(countries)
                .contactNames(contactNames)
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
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
