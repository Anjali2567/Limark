package ai.leadplus.api.v1.common;

import ai.leadplus.application.leadsearchhistories.LeadSearchHistoryDto;
import ai.leadplus.domain.leadcompanies.EmployeeRange;
import ai.leadplus.domain.common.LeadType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadSearchHistoryRequest {
    private String title;
    private Integer resultCount;
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

    public LeadSearchHistoryDto toDto(Long userId, LeadType type) {
        return LeadSearchHistoryDto.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .resultCount(resultCount)
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
                .build();
    }
}
