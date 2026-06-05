package ai.leadplus.domain.emailsequencetemplates;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailSequenceTemplateStep {
    private int stepNumber;
    private String subject;
    private String bodyTemplate;
    private int delayDays;
}
