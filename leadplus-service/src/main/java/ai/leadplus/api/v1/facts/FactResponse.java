package ai.leadplus.api.v1.facts;

import ai.leadplus.application.facts.FactDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactResponse {

    private Long id;
    private String fact;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FactResponse toResponse(FactDto dto) {
        return FactResponse.builder()
                .id(dto.getId())
                .fact(dto.getFact())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}