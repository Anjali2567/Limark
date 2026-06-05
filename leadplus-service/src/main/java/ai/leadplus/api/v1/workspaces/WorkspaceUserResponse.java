package ai.leadplus.api.v1.workspaces;

import ai.leadplus.application.workspaceuser.WorkspaceUserDetailsDto;
import ai.leadplus.application.workspaceuser.WorkspaceUserDto;
import ai.leadplus.domain.workspaceusers.WorkspaceUserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkspaceUserResponse {
    private Long id;
    private Long tenantId;
    private Long workspaceId;
    private String workspaceName;
    private Long userId;
    private WorkspaceUserRole role;

    public static WorkspaceUserResponse fromDto(WorkspaceUserDto workspaceUserDto) {
        return WorkspaceUserResponse.builder()
                .id(workspaceUserDto.getId())
                .tenantId(workspaceUserDto.getTenantId())
                .workspaceId(workspaceUserDto.getWorkspaceId())
                .userId(workspaceUserDto.getUserId())
                .role(workspaceUserDto.getRole())
                .build();
    }

    public static WorkspaceUserResponse fromDetailsDto(WorkspaceUserDetailsDto workspaceUserDto) {
        return WorkspaceUserResponse.builder()
                .id(workspaceUserDto.getId())
                .tenantId(workspaceUserDto.getTenantId())
                .workspaceId(workspaceUserDto.getWorkspaceId())
                .workspaceName(workspaceUserDto.getWorkspaceName())
                .userId(workspaceUserDto.getUserId())
                .role(workspaceUserDto.getRole())
                .build();
    }
}
