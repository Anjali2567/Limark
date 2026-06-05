package ai.leadplus.api.v1.leadlists;

import ai.leadplus.application.leadlists.LeadListDto;
import ai.leadplus.domain.common.LeadType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LeadListResponse {

    private Long id;
    private Long tenantId;
    private Long workspaceId;
    private String name;
    private LeadType type;
    private List<Long> sourceIds;
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static LeadListResponse fromDto(LeadListDto dto) {
        return LeadListResponse.builder()
                .id(dto.getId())
                .tenantId(dto.getTenantId())
                .workspaceId(dto.getWorkspaceId())
                .name(dto.getName())
                .type(dto.getType())
                .sourceIds(dto.getSourceIds())
                .active(dto.isActive())
                .createdBy(dto.getCreatedBy())
                .createdAt(dto.getCreatedAt())
                .updatedBy(dto.getUpdatedBy())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
