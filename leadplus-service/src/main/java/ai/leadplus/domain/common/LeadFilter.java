package ai.leadplus.domain.common;

import ai.leadplus.domain.leadcompanies.EmployeeRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadFilter {
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
}
