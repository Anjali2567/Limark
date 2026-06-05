package ai.leadplus.api.v1.sequencetemplates;

import ai.leadplus.application.sequencetemplates.SequenceTemplateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SequenceTemplateRequest {
    private String name;
    private int stepCount;
    private List<Integer> defaultDelays;
    private String purpose;

    public SequenceTemplateDto toDto() {
        return SequenceTemplateDto.builder()
                .name(name)
                .stepCount(stepCount)
                .defaultDelays(defaultDelays)
                .purpose(purpose)
                .build();
    }
}
