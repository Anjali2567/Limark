package ai.leadplus.application.common;

import ai.leadplus.domain.leadcompanies.EmployeeRange;
import ai.leadplus.domain.common.TargetingCriteria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TargetingCriteriaDto {
    private String conversationId;
    private String searchQuery;
    private List<EmployeeRange> employeeRanges;
    private LocationFilterDto companyLocationFilter;
    private LocationFilterDto companyExcludeLocationFilter;
    private List<Long> companyIds;
    private LocationFilterDto contactLocationFilter;
    private List<String> jobTitles;
    private List<Long> contactIds;
    private long totalCompanies;
    private long totalContacts;

    public static TargetingCriteriaDto fromEntity(TargetingCriteria entity) {
        if (entity == null) {
            return null;
        }
        return TargetingCriteriaDto.builder()
                .conversationId(entity.getConversationId())
                .searchQuery(entity.getSearchQuery())
                .employeeRanges(entity.getEmployeeRanges())
                .companyLocationFilter(entity.getCompanyLocationFilter() != null
                        ? LocationFilterDto.fromEntity(entity.getCompanyLocationFilter()) : null)
                .companyExcludeLocationFilter(entity.getCompanyExcludeLocationFilter() != null
                        ? LocationFilterDto.fromEntity(entity.getCompanyExcludeLocationFilter()) : null)
                .companyIds(entity.getCompanyIds())
                .contactLocationFilter(entity.getContactLocationFilter() != null
                        ? LocationFilterDto.fromEntity(entity.getContactLocationFilter()) : null)
                .jobTitles(entity.getJobTitles())
                .contactIds(entity.getContactIds())
                .totalCompanies(entity.getTotalCompanies())
                .totalContacts(entity.getTotalContacts())
                .build();
    }

    public TargetingCriteria toEntity() {
        return TargetingCriteria.builder()
                .conversationId(conversationId)
                .searchQuery(searchQuery)
                .employeeRanges(employeeRanges)
                .companyLocationFilter(companyLocationFilter != null ? companyLocationFilter.toEntity() : null)
                .companyExcludeLocationFilter(companyExcludeLocationFilter != null ? companyExcludeLocationFilter.toEntity() : null)
                .companyIds(companyIds)
                .contactLocationFilter(contactLocationFilter != null ? contactLocationFilter.toEntity() : null)
                .jobTitles(jobTitles)
                .contactIds(contactIds)
                .totalCompanies(totalCompanies)
                .totalContacts(totalContacts)
                .build();
    }

    public String toCleanedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Targeting Criteria {");
        if (!CollectionUtils.isEmpty(employeeRanges)) {
            sb.append("\nEmployee Ranges: ").append(employeeRanges);
        }
        if (companyLocationFilter != null) {
            sb.append("\nCompany Location Filter: ").append(companyLocationFilter.toCleanedString());
        }
        if (contactLocationFilter != null) {
            sb.append("\nContact Location Filter: ").append(contactLocationFilter.toCleanedString());
        }
        if (!CollectionUtils.isEmpty(jobTitles)) {
            sb.append("\nJob Titles: ").append(String.join(", ", jobTitles));
        }
        if (totalCompanies > 0) {
            sb.append("\nTotal Companies: ").append(totalCompanies);
        }
        if (totalContacts > 0) {
            sb.append("\nTotal Contacts: ").append(totalContacts);
        }
        sb.append("}");
        return sb.toString().trim();
    }
}