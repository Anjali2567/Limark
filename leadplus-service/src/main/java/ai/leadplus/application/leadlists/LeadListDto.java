package ai.leadplus.application.leadlists;

import ai.leadplus.domain.common.LeadType;
import ai.leadplus.domain.leadlists.LeadList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class LeadListDto {

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

    public static LeadListDto fromEntity(LeadList entity) {
        return LeadListDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .workspaceId(entity.getWorkspaceId())
                .name(entity.getName())
                .type(entity.getType())
                .sourceIds(entity.getSourceIds())
                .active(entity.isActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public LeadList toEntity() {
        return LeadList.builder()
                .id(id)
                .tenantId(tenantId)
                .workspaceId(workspaceId)
                .name(name)
                .type(type)
                .sourceIds(sourceIds)
                .active(active)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
