package ai.leadplus.application.workspaceuser;

import ai.leadplus.domain.workspaceusers.WorkspaceUser;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WorkspaceUserDetailsDto extends WorkspaceUserDto {
    private String workspaceName;

    public static WorkspaceUserDetailsDto fromDto(WorkspaceUser workspaceUser, String workspaceName) {
        return WorkspaceUserDetailsDto.builder()
                .id(workspaceUser.getId())
                .tenantId(workspaceUser.getTenantId())
                .workspaceId(workspaceUser.getWorkspaceId())
                .userId(workspaceUser.getUserId())
                .role(workspaceUser.getWorkspaceUserRole())
                .active(workspaceUser.isActive())
                .createdBy(workspaceUser.getCreatedBy())
                .createdAt(workspaceUser.getCreatedAt())
                .updatedBy(workspaceUser.getUpdatedBy())
                .updatedAt(workspaceUser.getUpdatedAt())
                .workspaceName(workspaceName)
                .build();
    }
}
