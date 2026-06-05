package ai.leadplus.application.promptspecifications;

import ai.leadplus.domain.promptspecifications.PromptSpecification;
import ai.leadplus.domain.promptspecifications.PromptSpecificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromptSpecificationDto {
    private Long id;
    private PromptSpecificationType type;
    private String promptTemplate;
    private Long createdBy;
    private LocalDateTime createdAt;

    public static PromptSpecificationDto fromEntity(PromptSpecification promptSpecification) {
        return PromptSpecificationDto.builder()
                .id(promptSpecification.getId())
                .type(promptSpecification.getType())
                .promptTemplate(promptSpecification.getPromptTemplate())
                .createdBy(promptSpecification.getCreatedBy())
                .createdAt(promptSpecification.getCreatedAt())
                .build();
    }

    public PromptSpecification toEntity() {
        return PromptSpecification.builder()
                .id(id)
                .type(type)
                .promptTemplate(promptTemplate)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .build();
    }
}
