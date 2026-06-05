package ai.leadplus.domain.common;

import ai.leadplus.domain.leadcompanies.EmployeeRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TargetingCriteria {
    private String conversationId;
    private String searchQuery;
    private List<EmployeeRange> employeeRanges;
    private LocationFilter companyLocationFilter;
    private LocationFilter companyExcludeLocationFilter;
    private List<Long> companyIds;
    private LocationFilter contactLocationFilter;
    private List<String> jobTitles;
    private List<Long> contactIds;
    private long totalCompanies;
    private long totalContacts;
}