package ai.leadplus.api.v1.promptspecifications;

import ai.leadplus.application.promptspecifications.PromptSpecificationDto;
import ai.leadplus.domain.promptspecifications.PromptSpecificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromptSpecificationRequest {
    private PromptSpecificationType type;
    private String promptTemplate;

    public PromptSpecificationDto toDto() {
        return PromptSpecificationDto.builder()
                .type(type)
                .promptTemplate(promptTemplate)
                .build();
    }
}
