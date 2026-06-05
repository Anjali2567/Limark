package ai.leadplus.application.workspaceuser;

import ai.leadplus.application.users.UserDto;
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
public class WorkspaceUserListDto {
    private Long id;
    private String name;
    private String email;
    private WorkspaceUserRole workspaceUserRole;
    private WorkspaceUserStatus workspaceUserStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WorkspaceUserListDto from(WorkspaceUserDto workspaceUser, UserDto userDto) {
        return WorkspaceUserListDto.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .workspaceUserRole(workspaceUser.getRole())
                .workspaceUserStatus(workspaceUser.getStatus())
                .createdAt(workspaceUser.getCreatedAt())
                .updatedAt(workspaceUser.getUpdatedAt())
                .build();
    }
}
