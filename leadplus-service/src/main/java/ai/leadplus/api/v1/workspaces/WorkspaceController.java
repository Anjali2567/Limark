package ai.leadplus.api.v1.workspaces;

import ai.leadplus.api.common.TenantValidator;
import ai.leadplus.api.common.WorkspaceValidator;
import ai.leadplus.api.common.UserValidator;
import ai.leadplus.application.workspaces.WorkspaceDto;
import ai.leadplus.application.workspaces.WorkspaceService;
import ai.leadplus.application.workspaceuser.WorkspaceUserDto;
import ai.leadplus.application.workspaceuser.WorkspaceUserListDto;
import ai.leadplus.application.workspaceuser.WorkspaceUserService;
import ai.leadplus.domain.users.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/tenants/{tenantId}/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspaces", description = "APIs for managing workspaces")
public class WorkspaceController {

    private final TenantValidator tenantValidator;
    private final WorkspaceValidator workspaceValidator;
    private final UserValidator userValidator;
    private final WorkspaceService workspaceService;
    private final WorkspaceUserService workspaceUserService;

    @Operation(summary = "Get a workspace by id", description = "Retrieve the details of a workspace by its ID")
    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceResponse> getWorkspaceById(@PathVariable Long tenantId,
                                                               @PathVariable Long workspaceId) {
        log.info("[GET] Received request to get Workspace with ID: {}", workspaceId);
        WorkspaceDto workspaceDto = workspaceService.getWorkspaceById(workspaceId);
        WorkspaceResponse workspaceResponse = WorkspaceResponse.fromDto(workspaceDto);
        log.info("[GET] Retrieved Workspace with ID: {}", workspaceResponse.getId());
        return ResponseEntity.ok(workspaceResponse);
    }

    @Operation(summary = "Update a workspace", description = "Update the details of an existing workspace by its ID")
    @PutMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceResponse> updateWorkspace(@PathVariable Long tenantId,
                                                              @PathVariable Long workspaceId,
                                                              @RequestBody @Valid WorkspaceRequest workspaceRequest) {
        log.info("[PUT] Received request to update Workspace with ID: {}", workspaceId);
        WorkspaceDto workspaceDto = workspaceService.updateWorkspace(workspaceId, workspaceRequest.toDto());
        WorkspaceResponse workspaceResponse = WorkspaceResponse.fromDto(workspaceDto);
        log.info("[PUT] Updated Workspace with ID: {}", workspaceResponse.getId());
        return ResponseEntity.ok(workspaceResponse);
    }

    @Operation(summary = "Invite a member to workspace")
    @PostMapping("/{workspaceId}/invite")
    public ResponseEntity<WorkspaceUserResponse> inviteMemberToWorkspace(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @RequestBody @Valid WorkspaceUserInviteRequest workspaceUserInviteRequest) {
        validateWorkspaceManagementAccess(tenantId, workspaceId);
        log.info("[POST] Received request to invite member to workspace with ID: {}", workspaceId);

        WorkspaceUserDto workspaceUserDto = workspaceUserService.inviteMember(tenantId, workspaceId, workspaceUserInviteRequest.toDto());
        WorkspaceUserResponse workspaceUserResponse = WorkspaceUserResponse.fromDto(workspaceUserDto);
        log.info("[POST] Successfully invited member to workspace with ID: {}", workspaceId);
        return ResponseEntity.ok(workspaceUserResponse);
    }

    @Operation(summary = "Get all Workspace Users")
    @GetMapping("/{workspaceId}/users")
    public ResponseEntity<Page<WorkspaceUserListResponse>> getAllUsers(@PathVariable Long tenantId, @PathVariable Long workspaceId, Pageable pageable) {
        log.info("[GET] Received request to get all workspace users for workspace: {}", workspaceId);
        Page<WorkspaceUserListDto> workspaceUserDtoPage = workspaceUserService.searchWorkspaceUsers(workspaceId, pageable);
        Page<WorkspaceUserListResponse> workspaceUserResponses = workspaceUserDtoPage.map(WorkspaceUserListResponse::from);
        log.info("[GET] Returned workspace users for workspace: {}", workspaceUserResponses.getSize());
        return ResponseEntity.ok(workspaceUserResponses);
    }

    @Operation(summary = "Revoke Access to the workspace")
    @DeleteMapping("/{workspaceId}/users/{userId}")
    public ResponseEntity<Void> revokeUserAccess(@PathVariable Long tenantId, @PathVariable Long workspaceId, @PathVariable Long userId) {
        validateWorkspaceManagementAccess(tenantId, workspaceId);
        log.info("[DELETE] Received request to revoke access to member with ID: {}", userId);
        workspaceUserService.revokeAccess(workspaceId, userId);
        log.info("[DELETE] Successfully revoked member with ID: {}", userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update workspace member role")
    @PutMapping("/{workspaceId}/users/{userId}/role")
    public ResponseEntity<WorkspaceUserResponse> updateUserRole(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @PathVariable Long userId,
            @Valid @RequestBody WorkspaceUserRoleRequest request) {
        validateWorkspaceManagementAccess(tenantId, workspaceId);
        log.info("[PUT] Received request to update role for workspace user with ID: {}", userId);
        WorkspaceUserDto workspaceUserDto = workspaceUserService.updateWorkspaceUserRole(workspaceId, userId, request.getRole());
        WorkspaceUserResponse workspaceUserResponse = WorkspaceUserResponse.fromDto(workspaceUserDto);
        log.info("[PUT] Updated role for workspace user with ID: {}", userId);
        return ResponseEntity.ok(workspaceUserResponse);
    }

    private void validateWorkspaceManagementAccess(Long tenantId, Long workspaceId) {
        boolean isTenantOwner = userValidator.getAuthenticatedUser().getRoles() != null
                && userValidator.getAuthenticatedUser().getRoles().contains(UserRole.TENANT_OWNER);
        if (isTenantOwner) {
            tenantValidator.validateAuthenticatedUserByTenantId(tenantId);
            return;
        }
        workspaceValidator.validateIsWorkspaceOwner(workspaceId);
    }
}
