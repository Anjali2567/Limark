package ai.leadplus.api.v1.facts;

import ai.leadplus.application.facts.FactDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactRequest {

    @NotBlank
    private String fact;

    public FactDto toDto() {
        return FactDto.builder()
                .fact(fact)
                .build();
    }
}