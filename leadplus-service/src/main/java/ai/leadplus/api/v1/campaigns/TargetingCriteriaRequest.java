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
public class TargetingCriteriaRequest {
    private List<EmployeeRange> employeeRanges;
    private LocationFilterDto companyLocationFilter;
    private LocationFilterDto contactLocationFilter;
    private List<String> jobTitles;

    public TargetingCriteriaDto toDto() {
        return TargetingCriteriaDto.builder()
                .employeeRanges(employeeRanges)
                .companyLocationFilter(companyLocationFilter)
                .contactLocationFilter(contactLocationFilter)
                .jobTitles(jobTitles)
                .build();

    }
}
