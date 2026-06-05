package ai.leadplus.api.v1.leadqueries;

import ai.leadplus.application.leadqueries.LeadQueryDto;
import ai.leadplus.domain.leadqueries.LeadQueryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadQueryResponse {
    private LeadQueryType type;
    private String value;

    public static LeadQueryResponse fromDto(LeadQueryDto dto) {
        return LeadQueryResponse.builder()
                .type(dto.getType())
                .value(dto.getValue())
                .build();
    }
}
