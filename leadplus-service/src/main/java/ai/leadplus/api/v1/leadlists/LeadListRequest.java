package ai.leadplus.api.v1.leadlists;

import ai.leadplus.application.leadlists.LeadListDto;
import ai.leadplus.domain.common.LeadType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadListRequest {

    private String name;

    @NotNull(message = "Lead type is required")
    private LeadType type;

    @NotNull(message = "Source IDs are required")
    private List<Long> sourceIds;

    public LeadListDto toDto() {
        return LeadListDto.builder()
                .name(name)
                .type(type)
                .sourceIds(sourceIds)
                .build();
    }
}
