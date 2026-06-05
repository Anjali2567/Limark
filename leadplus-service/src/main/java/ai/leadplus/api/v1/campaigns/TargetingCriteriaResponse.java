package ai.leadplus.api.v1.campaigns;

import ai.leadplus.application.common.LocationFilterDto;
import ai.leadplus.application.common.TargetingCriteriaDto;
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
public class TargetingCriteriaResponse {
    private String searchQuery;
    private List<EmployeeRange> employeeRanges;
    private LocationFilterDto companyLocationFilter;
    private LocationFilterDto companyExcludeLocationFilter;
    private LocationFilterDto contactLocationFilter;
    private List<String> jobTitles;

    public static TargetingCriteriaResponse fromDto(TargetingCriteriaDto dto) {
        if (dto == null) {
            return null;
        }
        return TargetingCriteriaResponse.builder()
                .searchQuery(dto.getSearchQuery())
                .employeeRanges(dto.getEmployeeRanges())
                .companyLocationFilter(dto.getCompanyLocationFilter())
                .companyExcludeLocationFilter(dto.getCompanyExcludeLocationFilter())
                .contactLocationFilter(dto.getContactLocationFilter())
                .jobTitles(dto.getJobTitles())
                .build();
    }
}
