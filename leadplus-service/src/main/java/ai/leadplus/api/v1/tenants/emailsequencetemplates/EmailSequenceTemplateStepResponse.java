package ai.leadplus.api.v1.tenants.emailsequencetemplates;

import ai.leadplus.application.emailsequencetemplates.EmailSequenceTemplateStepDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailSequenceTemplateStepResponse {

    private int stepNumber;
    private String subject;
    private String bodyTemplate;
    private int delayDays;

    public static EmailSequenceTemplateStepResponse fromDto(EmailSequenceTemplateStepDto dto) {
        return EmailSequenceTemplateStepResponse.builder()
                .stepNumber(dto.getStepNumber())
                .subject(dto.getSubject())
                .bodyTemplate(dto.getBodyTemplate())
                .delayDays(dto.getDelayDays())
                .build();
    }
}
