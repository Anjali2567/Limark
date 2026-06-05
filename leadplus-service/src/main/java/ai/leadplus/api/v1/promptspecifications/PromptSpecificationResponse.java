package ai.leadplus.api.v1.promptspecifications;


import ai.leadplus.application.promptspecifications.PromptSpecificationDto;
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
public class PromptSpecificationResponse {
    private Long id;
    private PromptSpecificationType type;
    private String promptTemplate;
    private Long createdBy;
    private LocalDateTime createdAt;

    public static PromptSpecificationResponse fromDto(PromptSpecificationDto promptSpecificationDto) {
        return PromptSpecificationResponse.builder()
                .id(promptSpecificationDto.getId())
                .type(promptSpecificationDto.getType())
                .promptTemplate(promptSpecificationDto.getPromptTemplate())
                .createdBy(promptSpecificationDto.getCreatedBy())
                .createdAt(promptSpecificationDto.getCreatedAt())
                .build();
    }
}
