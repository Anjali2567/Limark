package ai.leadplus.api.v1.tenants.emailsequencetemplates;

import ai.leadplus.application.emailsequencetemplates.EmailSequenceTemplateStepDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSequenceTemplateStepRequest {

    private int stepNumber;
    private String subject;
    private String bodyTemplate;
    private int delayDays;

    public EmailSequenceTemplateStepDto toDto() {
        return EmailSequenceTemplateStepDto.builder()
                .stepNumber(stepNumber)
                .subject(subject)
                .bodyTemplate(bodyTemplate)
                .delayDays(delayDays)
                .build();
    }
}
