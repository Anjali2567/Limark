package ai.leadplus.application.facts;

import ai.leadplus.domain.facts.Fact;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactDto {
    private Long id;
    private String fact;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FactDto toDto(Fact fact) {
        return FactDto.builder()
                .id(fact.getId())
                .fact(fact.getFact())
                .createdAt(fact.getCreatedAt())
                .updatedAt(fact.getUpdatedAt())
                .build();
    }

    public Fact toEntity() {
        return Fact.builder()
                .id(id)
                .fact(fact)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}