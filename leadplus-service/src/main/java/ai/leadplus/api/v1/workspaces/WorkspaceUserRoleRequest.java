package ai.leadplus.api.v1.workspaces;

import ai.leadplus.domain.workspaceusers.WorkspaceUserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceUserRoleRequest {

    @NotNull
    private WorkspaceUserRole role;
}
