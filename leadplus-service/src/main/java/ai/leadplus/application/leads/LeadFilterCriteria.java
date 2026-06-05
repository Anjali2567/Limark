package ai.leadplus.application.leads;

import ai.leadplus.domain.common.LeadFilter;
import ai.leadplus.domain.leadcompanies.EmployeeRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadFilterCriteria {
    private List<Long> companyIds;
    private List<String> contactNames;
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
    private List<String> toolsServices;
    private List<String> titles;
    private List<String> seniority;
    private List<String> departments;
    private List<String> postalCodes;
    private List<String> sicCodes;
    private List<String> naicsCodes;
    private List<String> contactSegments;
    private List<String> bdNames;
    private List<String> isrNames;
    private List<String> priorities;
    private List<String> titleCategories;
    private boolean aggregateTechSearch;
    private boolean campaignEligibleOnly;
    private Set<Long> excludeContactIds;

    public static LeadFilterCriteria fromEntity(LeadFilter leadFilter) {
        if (leadFilter == null) {
            return null;
        }
        return LeadFilterCriteria.builder()
                .cities(mergeLists(leadFilter.getCompanyCities(), leadFilter.getCities()))
                .states(mergeLists(leadFilter.getCompanyStates(), leadFilter.getStates()))
                .countries(mergeLists(leadFilter.getCompanyCountries(), leadFilter.getCountries()))
                .companyNames(leadFilter.getCompanyNames())
                .companyCities(leadFilter.getCompanyCities())
                .companyStates(leadFilter.getCompanyStates())
                .companyCountries(leadFilter.getCompanyCountries())
                .regions(leadFilter.getRegions())
                .keywords(leadFilter.getKeywords())
                .industries(leadFilter.getIndustries())
                .employeeRanges(leadFilter.getEmployeeRanges())
                .revenueRanges(leadFilter.getRevenueRanges())
                .technologies(leadFilter.getTechnologies())
                .toolsServices(leadFilter.getToolsServices())
                .titles(leadFilter.getTitles())
                .seniority(leadFilter.getSeniority())
                .departments(leadFilter.getDepartments())
                .postalCodes(leadFilter.getPostalCodes())
                .sicCodes(leadFilter.getSicCodes())
                .naicsCodes(leadFilter.getNaicsCodes())
                .build();
    }

    public boolean hasCompanyFilters() {
        return !CollectionUtils.isEmpty(companyNames)
                || !CollectionUtils.isEmpty(industries)
                || !CollectionUtils.isEmpty(employeeRanges)
                || !CollectionUtils.isEmpty(revenueRanges)
                || !CollectionUtils.isEmpty(sicCodes)
                || !CollectionUtils.isEmpty(naicsCodes)
                || !CollectionUtils.isEmpty(keywords)
                || !CollectionUtils.isEmpty(technologies)
                || !CollectionUtils.isEmpty(toolsServices)
                || !CollectionUtils.isEmpty(regions)
                || !CollectionUtils.isEmpty(companyCities)
                || !CollectionUtils.isEmpty(companyStates)
                || !CollectionUtils.isEmpty(companyCountries)
                || !CollectionUtils.isEmpty(postalCodes);
    }

    public boolean hasContactFilters() {
        return !CollectionUtils.isEmpty(contactNames)
                || !CollectionUtils.isEmpty(cities)
                || !CollectionUtils.isEmpty(states)
                || !CollectionUtils.isEmpty(countries)
                || !CollectionUtils.isEmpty(titles)
                || !CollectionUtils.isEmpty(seniority)
                || !CollectionUtils.isEmpty(departments)
                || !CollectionUtils.isEmpty(contactSegments)
                || hasTenantMetadataFilters();
    }

    public boolean hasTenantMetadataFilters() {
        return !CollectionUtils.isEmpty(bdNames)
                || !CollectionUtils.isEmpty(isrNames)
                || !CollectionUtils.isEmpty(priorities)
                || !CollectionUtils.isEmpty(titleCategories);
    }

    public boolean hasAnyFilters() {
        return hasCompanyFilters() || hasContactFilters();
    }

    public LeadFilter toEntity() {
        return LeadFilter.builder()
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
                .toolsServices(toolsServices)
                .titles(titles)
                .seniority(seniority)
                .departments(departments)
                .postalCodes(postalCodes)
                .sicCodes(sicCodes)
                .naicsCodes(naicsCodes)
                .build();
    }

    private static <T> List<T> mergeLists(List<T> first, List<T> second) {
        List<T> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(first)) result.addAll(first);
        if (!CollectionUtils.isEmpty(second)) result.addAll(second);
        return result;
    }
}
