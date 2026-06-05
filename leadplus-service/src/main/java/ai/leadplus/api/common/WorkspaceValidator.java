package ai.leadplus.api.common;

import ai.leadplus.application.exception.UnauthorizedException;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.workspaceuser.WorkspaceUserDto;
import ai.leadplus.application.workspaceuser.WorkspaceUserService;
import ai.leadplus.domain.workspaceusers.WorkspaceUserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WorkspaceValidator {

    private final UserValidator userValidator;
    private final WorkspaceUserService workspaceUserService;

    public UserDto validateIsWorkspaceOwner(Long workspaceId) {
        UserDto user = userValidator.getAuthenticatedUser();

        WorkspaceUserDto workspaceUser = workspaceUserService.findByWorkspaceIdAndUserId(workspaceId, user.getId());
        if (!Objects.equals(workspaceUser.getRole(), WorkspaceUserRole.OWNER)) {
            throw new UnauthorizedException("Only the workspace owner is allowed to perform this action");
        }
        return  user;
    }
}
