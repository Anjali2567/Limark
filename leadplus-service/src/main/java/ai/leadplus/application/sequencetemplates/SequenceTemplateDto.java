package ai.leadplus.application.sequencetemplates;

import ai.leadplus.domain.sequencetemplates.SequenceTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SequenceTemplateDto {
    private Long id;
    private String name;
    private int stepCount;
    private List<Integer> defaultDelays;
    private String purpose;

    public static SequenceTemplateDto fromEntity(SequenceTemplate sequenceTemplate) {
        return SequenceTemplateDto.builder()
                .id(sequenceTemplate.getId())
                .name(sequenceTemplate.getName())
                .stepCount(sequenceTemplate.getStepCount())
                .defaultDelays(sequenceTemplate.getDefaultDelays())
                .purpose(sequenceTemplate.getPurpose())
                .build();
    }

    public SequenceTemplate toEntity() {
        return SequenceTemplate.builder()
                .id(id)
                .name(name)
                .stepCount(stepCount)
                .defaultDelays(defaultDelays)
                .purpose(purpose)
                .build();
    }
}
