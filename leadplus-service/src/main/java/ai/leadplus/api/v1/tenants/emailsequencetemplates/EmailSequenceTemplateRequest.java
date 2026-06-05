package ai.leadplus.api.v1.tenants.emailsequencetemplates;

import ai.leadplus.application.emailsequencetemplates.EmailSequenceTemplateDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSequenceTemplateRequest {

    @NotBlank
    private String name;

    private String description;

    @NotEmpty
    @Valid
    private List<EmailSequenceTemplateStepRequest> steps;

    public EmailSequenceTemplateDto toDto() {
        return EmailSequenceTemplateDto.builder()
                .name(name)
                .description(description)
                .steps(steps == null ? null : steps.stream()
                        .map(EmailSequenceTemplateStepRequest::toDto)
                        .toList())
                .build();
    }
}
