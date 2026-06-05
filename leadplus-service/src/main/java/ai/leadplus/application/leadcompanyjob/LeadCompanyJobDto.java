package ai.leadplus.application.leadcompanyjob;

import ai.leadplus.domain.leadcompanyjobs.LeadCompanyJob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadCompanyJobDto {

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
    private List<String> technologies;
    private List<String> tools;
    private List<String> services;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LeadCompanyJobDto fromEntity(LeadCompanyJob job) {
        if (job == null) {
            return null;
        }
        return LeadCompanyJobDto.builder()
                .id(job.getId())
                .leadCompanyId(job.getLeadCompanyId())
                .type(job.getType())
                .title(job.getTitle())
                .skills(job.getSkills())
                .jobUrl(job.getJobUrl())
                .applyUrl(job.getApplyUrl())
                .benefits(job.getBenefits())
                .location(job.getLocation())
                .department(job.getDepartment())
                .postedDate(job.getPostedDate())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .technologies(job.getTechnologies())
                .tools(job.getTools())
                .services(job.getServices())
                .active(job.isActive())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    public LeadCompanyJob toEntity() {
        return LeadCompanyJob.builder()
                .id(id)
                .leadCompanyId(leadCompanyId)
                .type(type)
                .title(title)
                .skills(skills)
                .jobUrl(jobUrl)
                .applyUrl(applyUrl)
                .benefits(benefits)
                .location(location)
                .department(department)
                .postedDate(postedDate)
                .description(description)
                .requirements(requirements)
                .technologies(technologies)
                .tools(tools)
                .services(services)
                .active(active)
                .build();
    }
}
