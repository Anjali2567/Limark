package ai.leadplus.api.v1.messages;

import ai.leadplus.application.common.LocationFilterDto;
import ai.leadplus.application.common.TargetingCriteriaDto;
import ai.leadplus.domain.leadcompanies.EmployeeRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetingCriteriaCountResponse {

    private List<EmployeeRange> employeeRanges;
    private LocationFilterDto companyLocationFilter;
    private LocationFilterDto companyExcludeLocationFilter;
    private long noOfCompanies;
    private long noOfContacts;
    private LocationFilterDto contactLocationFilter;
    private List<String> jobTitles;

    public static TargetingCriteriaCountResponse fromDto(TargetingCriteriaDto dto) {
        if (dto == null) return null;
        long noOfCompanies = dto.getTotalCompanies() > 0
                ? dto.getTotalCompanies()
                : (CollectionUtils.isEmpty(dto.getCompanyIds()) ? 0 : dto.getCompanyIds().size());

        long noOfContacts = dto.getTotalContacts() > 0
                ? dto.getTotalContacts()
                : (CollectionUtils.isEmpty(dto.getContactIds()) ? 0 : dto.getContactIds().size());

        return TargetingCriteriaCountResponse.builder()
                .employeeRanges(dto.getEmployeeRanges())
                .companyLocationFilter(dto.getCompanyLocationFilter())
                .companyExcludeLocationFilter(dto.getCompanyExcludeLocationFilter())
                .contactLocationFilter(dto.getContactLocationFilter())
                .jobTitles(dto.getJobTitles())
                .noOfCompanies(noOfCompanies)
                .noOfContacts(noOfContacts)
                .build();
    }
}