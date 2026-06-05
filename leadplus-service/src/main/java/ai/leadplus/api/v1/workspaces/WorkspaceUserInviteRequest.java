package ai.leadplus.api.v1.workspaces;

import ai.leadplus.application.workspaceuser.WorkspaceUserInviteDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkspaceUserInviteRequest {
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;

    private String name;

    public WorkspaceUserInviteDto toDto() {
        return WorkspaceUserInviteDto.builder()
                .name(name)
                .email(email)
                .build();
    }
}
