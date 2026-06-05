package ai.leadplus.api.v1.leads;

import ai.leadplus.application.leadcompanyjob.LeadCompanyJobDto;
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
public class LeadCompanyJobResponse {

    private Long id;
    private Long leadCompanyId;
    private String type;
    private String title;
    private List<String> skills;
    private String jobUrl;
    private String applyUrl;
    private List<String> benefits;
    private String location;
    private String department;
    private LocalDateTime postedDate;
    private String description;
    private List<String> requirements;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LeadCompanyJobResponse fromDto(LeadCompanyJobDto dto) {
        if (dto == null) {
            return null;
        }
        return LeadCompanyJobResponse.builder()
                .id(dto.getId())
                .leadCompanyId(dto.getLeadCompanyId())
                .type(dto.getType())
                .title(dto.getTitle())
                .skills(dto.getSkills())
                .jobUrl(dto.getJobUrl())
                .applyUrl(dto.getApplyUrl())
                .benefits(dto.getBenefits())
                .location(dto.getLocation())
                .department(dto.getDepartment())
                .postedDate(dto.getPostedDate())
                .description(dto.getDescription())
                .requirements(dto.getRequirements())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
