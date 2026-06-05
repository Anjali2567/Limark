package ai.leadplus.application.campaigngenerator.tools;

import ai.leadplus.application.campaignchatmemory.CampaignChatMemoryDto;
import ai.leadplus.application.campaigns.CampaignDto;
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
public class CleanedCampaign {
    private String name;
    private String industry;
    private String searchQuery;
    private List<EmployeeRange> employeeRanges;
    private LocationFilterDto companyLocationFilter;
    private LocationFilterDto companyExcludeLocationFilter;
    private LocationFilterDto contactLocationFilter;
    private List<String> jobTitles;

    public static CleanedCampaign fromDto(CampaignDto dto) {
        TargetingCriteriaDto targetingCriteria = dto.getTargetingCriteria() != null ? dto.getTargetingCriteria() : new TargetingCriteriaDto();
        return CleanedCampaign.builder()
                .name(dto.getName())
                .industry(dto.getIndustry())
                .searchQuery(targetingCriteria.getSearchQuery())
                .employeeRanges(targetingCriteria.getEmployeeRanges())
                .companyLocationFilter(targetingCriteria.getCompanyLocationFilter())
                .companyExcludeLocationFilter(targetingCriteria.getCompanyExcludeLocationFilter())
                .contactLocationFilter(targetingCriteria.getContactLocationFilter())
                .jobTitles(targetingCriteria.getJobTitles())
                .build();
    }

    public static CleanedCampaign fromDto(CampaignChatMemoryDto dto) {
        TargetingCriteriaDto targetingCriteria = dto.getTargetingCriteria() != null ? dto.getTargetingCriteria() : new TargetingCriteriaDto();
        return CleanedCampaign.builder()
                .name(dto.getName())
                .industry(dto.getIndustry())
                .searchQuery(targetingCriteria.getSearchQuery())
                .employeeRanges(targetingCriteria.getEmployeeRanges())
                .companyLocationFilter(targetingCriteria.getCompanyLocationFilter())
                .companyExcludeLocationFilter(targetingCriteria.getCompanyExcludeLocationFilter())
                .contactLocationFilter(targetingCriteria.getContactLocationFilter())
                .jobTitles(targetingCriteria.getJobTitles())
                .build();
    }
}
