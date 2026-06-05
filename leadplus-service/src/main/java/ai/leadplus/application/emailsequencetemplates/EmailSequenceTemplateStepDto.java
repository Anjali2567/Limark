package ai.leadplus.application.emailsequencetemplates;

import ai.leadplus.domain.emailsequencetemplates.EmailSequenceTemplateStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSequenceTemplateStepDto {

    private int stepNumber;
    private String subject;
    private String bodyTemplate;
    private int delayDays;

    public static EmailSequenceTemplateStepDto fromEntity(EmailSequenceTemplateStep entity) {
        if (entity == null) {
            return null;
        }
        return EmailSequenceTemplateStepDto.builder()
                .stepNumber(entity.getStepNumber())
                .subject(entity.getSubject())
                .bodyTemplate(entity.getBodyTemplate())
                .delayDays(entity.getDelayDays())
                .build();
    }

    public EmailSequenceTemplateStep toEntity() {
        return EmailSequenceTemplateStep.builder()
                .stepNumber(stepNumber)
                .subject(subject)
                .bodyTemplate(bodyTemplate)
                .delayDays(delayDays)
                .build();
    }
}
