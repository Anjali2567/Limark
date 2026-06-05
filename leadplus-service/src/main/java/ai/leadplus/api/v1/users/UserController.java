package ai.leadplus.api.v1.users;

import ai.leadplus.api.common.UserValidator;
import ai.leadplus.api.v1.workspaces.WorkspaceUserResponse;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.application.workspaceuser.WorkspaceUserDetailsDto;
import ai.leadplus.application.workspaceuser.WorkspaceUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User Management APIs")
public class UserController {

    private final UserService userService;
    private final WorkspaceUserService workspaceUserService;
    private final UserValidator userValidator;

    @Operation(summary = "Get Logged In user Details")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getLoggedInUserDetails() {
        log.info("[GET][auth/me] Request received to fetch logged-in user details");
        UserResponse userResponse = UserResponse.fromDto(userValidator.getAuthenticatedUser());
        log.info("[GET][auth/me] Successfully returned logged-in user details with userId : {}", userResponse.getId());
        return ResponseEntity.ok(userResponse);
    }

    @Operation(summary = "Update user")
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UpdateUserRequest request) {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[PUT][users/{}] Request received to update user", userId);
        UserDto updated = userService.updateUser(Long.parseLong(userId), request.toDto());
        UserResponse response = UserResponse.fromDto(updated);
        log.info("[PUT][users/{}] Successfully updated user", userId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user password")
    @PutMapping("/me/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[PUT][users/{}/password] Request received to update password", userId);
        userService.updatePassword(Long.parseLong(userId), request.getOldPassword(), request.getNewPassword());
        log.info("[PUT][users/{}/password] Successfully updated password", userId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser() {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[DELETE][users/{}] Request received to delete user", userId);
        userService.deleteUser(Long.parseLong(userId));
        log.info("[DELETE][users/{}] Successfully deleted user", userId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all user workspaces")
    @GetMapping("/me/workspaces")
    public ResponseEntity<List<WorkspaceUserResponse>> getUserWorkspaces() {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[GET] Fetching workspaces for userId={}", userId);
        List<WorkspaceUserDetailsDto> workspaceUsers = workspaceUserService.getAllUserWorkspaces(Long.parseLong(userId));
        List<WorkspaceUserResponse> response = workspaceUsers.stream().map(WorkspaceUserResponse::fromDetailsDto).toList();
        log.info("[GET] Successfully fetched workspaces for userId={}", userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Resend User Email verification")
    @PutMapping("/me/resend-email")
    public ResponseEntity<Void> resendUserEmailVerification() {
        log.info("[PUT] Received request to Resend User Email verification");
        String userId = userValidator.getAuthenticatedUserId();
        userService.resendEmailVerification(Long.parseLong(userId));
        log.info("[PUT] Resend User Email verification by ID: {}", userId);
        return ResponseEntity.noContent().build();
    }
}