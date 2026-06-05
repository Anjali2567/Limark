package ai.leadplus.api.v1.tenants.emailsequencetemplates;

import ai.leadplus.application.emailsequencetemplates.EmailSequenceTemplateDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EmailSequenceTemplateResponse {

    private Long id;
    private String name;
    private String description;
    private int stepCount;
    private List<EmailSequenceTemplateStepResponse> steps;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EmailSequenceTemplateResponse fromDto(EmailSequenceTemplateDto dto) {
        return EmailSequenceTemplateResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .stepCount(dto.getSteps() == null ? 0 : dto.getSteps().size())
                .steps(dto.getSteps() == null ? null : dto.getSteps().stream()
                        .map(EmailSequenceTemplateStepResponse::fromDto)
                        .toList())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
