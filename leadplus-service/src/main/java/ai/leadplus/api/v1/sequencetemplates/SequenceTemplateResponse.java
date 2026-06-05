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
public class SequenceTemplateResponse {
    private Long id;
    private String name;
    private int stepCount;
    private List<Integer> defaultDelays;
    private String purpose;

    public static SequenceTemplateResponse fromDto(SequenceTemplateDto dto) {
        return SequenceTemplateResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .stepCount(dto.getStepCount())
                .defaultDelays(dto.getDefaultDelays())
                .purpose(dto.getPurpose())
                .build();
    }
}
