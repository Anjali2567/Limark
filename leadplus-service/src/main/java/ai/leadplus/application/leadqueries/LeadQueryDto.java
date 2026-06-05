package ai.leadplus.application.leadqueries;

import ai.leadplus.domain.leadqueries.LeadQuery;
import ai.leadplus.domain.leadqueries.LeadQueryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadQueryDto {

    private Long id;
    private LeadQueryType type;
    private String value;
    private LocalDateTime createdAt;

    public static LeadQueryDto fromEntity(LeadQuery leadQuery) {
        return LeadQueryDto.builder()
                .id(leadQuery.getId())
                .type(leadQuery.getType())
                .value(leadQuery.getValue())
                .createdAt(leadQuery.getCreatedAt())
                .build();
    }

    public LeadQuery toEntity() {
        return LeadQuery.builder()
                .id(id)
                .type(type)
                .value(value)
                .createdAt(createdAt)
                .build();
    }
}
