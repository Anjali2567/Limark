package ai.leadplus.application.workspaceuser;

import ai.leadplus.domain.workspaceusers.WorkspaceUser;
import ai.leadplus.domain.workspaceusers.WorkspaceUserRole;
import ai.leadplus.domain.workspaceusers.WorkspaceUserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WorkspaceUserDto {
    private Long id;
    private Long tenantId;
    private Long workspaceId;
    private Long userId;
    private WorkspaceUserRole role;
    private WorkspaceUserStatus status;
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static WorkspaceUserDto fromEntity(WorkspaceUser workspaceUser) {
        return WorkspaceUserDto.builder()
                .id(workspaceUser.getId())
                .tenantId(workspaceUser.getTenantId())
                .workspaceId(workspaceUser.getWorkspaceId())
                .userId(workspaceUser.getUserId())
                .role(workspaceUser.getWorkspaceUserRole())
                .status(workspaceUser.getWorkspaceUserStatus())
                .active(workspaceUser.isActive())
                .createdBy(workspaceUser.getCreatedBy())
                .createdAt(workspaceUser.getCreatedAt())
                .updatedBy(workspaceUser.getUpdatedBy())
                .updatedAt(workspaceUser.getUpdatedAt())
                .build();
    }

    public WorkspaceUser toEntity() {
        return WorkspaceUser.builder()
                .id(id)
                .tenantId(tenantId)
                .workspaceId(workspaceId)
                .userId(userId)
                .workspaceUserRole(role)
                .workspaceUserStatus(status)
                .active(active)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
