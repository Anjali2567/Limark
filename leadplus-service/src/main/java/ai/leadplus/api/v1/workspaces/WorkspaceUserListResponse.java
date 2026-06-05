package ai.leadplus.api.v1.workspaces;

import ai.leadplus.application.workspaceuser.WorkspaceUserListDto;
import ai.leadplus.domain.workspaceusers.WorkspaceUserRole;
import ai.leadplus.domain.workspaceusers.WorkspaceUserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkspaceUserListResponse {
    private Long id;
    private String name;
    private String email;
    private WorkspaceUserRole workspaceUserRole;
    private WorkspaceUserStatus workspaceUserStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WorkspaceUserListResponse from(WorkspaceUserListDto workspaceUserListDto) {
        return WorkspaceUserListResponse.builder()
                .id(workspaceUserListDto.getId())
                .name(workspaceUserListDto.getName())
                .email(workspaceUserListDto.getEmail())
                .workspaceUserRole(workspaceUserListDto.getWorkspaceUserRole())
                .workspaceUserStatus(workspaceUserListDto.getWorkspaceUserStatus())
                .createdAt(workspaceUserListDto.getCreatedAt())
                .updatedAt(workspaceUserListDto.getUpdatedAt())
                .build();
    }
}
