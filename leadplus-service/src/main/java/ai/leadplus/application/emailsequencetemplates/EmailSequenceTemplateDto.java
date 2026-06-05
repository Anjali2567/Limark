package ai.leadplus.application.emailsequencetemplates;

import ai.leadplus.domain.emailsequencetemplates.EmailSequenceTemplate;
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
public class EmailSequenceTemplateDto {

    private Long id;
    private Long tenantId;
    private String name;
    private String description;
    private List<EmailSequenceTemplateStepDto> steps;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EmailSequenceTemplateDto fromEntity(EmailSequenceTemplate entity) {
        if (entity == null) return null;
        return EmailSequenceTemplateDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .steps(entity.getSteps() == null ? null : entity.getSteps().stream()
                        .map(EmailSequenceTemplateStepDto::fromEntity)
                        .toList())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public EmailSequenceTemplate toEntity() {
        return EmailSequenceTemplate.builder()
                .id(id)
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .steps(steps == null ? null : steps.stream()
                        .map(EmailSequenceTemplateStepDto::toEntity)
                        .toList())
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
