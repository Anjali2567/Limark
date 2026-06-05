package ai.leadplus.api.v1.leadlists;

import ai.leadplus.application.leadlists.LeadListSearchDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LeadListSearchResponse extends LeadListResponse{
    private int sourceCount;
    private String username;

    public static LeadListSearchResponse fromDto(LeadListSearchDto dto) {
        if (dto == null) return null;

        return LeadListSearchResponse.builder()
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
                .sourceCount(dto.getSourceCount())
                .username(dto.getUsername())
                .build();
    }
}
