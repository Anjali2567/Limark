package ai.leadplus.application.leadlists;

import ai.leadplus.domain.leadlists.LeadList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LeadListSearchDto extends LeadListDto {
    private int sourceCount;
    private String username;

    public static LeadListSearchDto fromEntity(LeadList entity, String username, int sourceCount) {
        return LeadListSearchDto.builder()
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
                .sourceCount(sourceCount)
                .username(username)
                .build();
    }
}
