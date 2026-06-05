package ai.leadplus.api.v1.admin.users;

import ai.leadplus.api.v1.users.UserResponse;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.domain.users.UserRole;
import ai.leadplus.domain.users.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin Users", description = "Admin User Management APIs")
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "Get all Users")
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllAdminUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<UserRole> userRoles,
            @RequestParam(required = false) List<UserStatus> userStatuses,
            Pageable pageable) {
        log.info("Get all Users");
        Page<UserDto> userPage = userService.searchUsers(null, query, userRoles, userStatuses, pageable);
        Page<UserResponse> responsePage = userPage.map(UserResponse::fromDto);
        log.info("Returning {} users", responsePage.getSize());
        return ResponseEntity.ok(responsePage);
    }

    @Operation(summary = "Get User by Id")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("[GET] Received request to get user with ID: {}", id);
        UserDto userDto = userService.getUserById(id);
        UserResponse response = UserResponse.fromDto(userDto);
        log.info("[GET] Returning user with ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Approve User")
    @PutMapping("/{id}/approve")
    public ResponseEntity<UserResponse> approveUser(@PathVariable Long id) {
        log.info("[PUT] Received request to approve user with ID: {}", id);
        UserDto userDto = userService.approveUser(id);
        UserResponse userResponse = UserResponse.fromDto(userDto);
        log.info("[PUT] Approved user with ID: {}", id);
        return ResponseEntity.ok(userResponse);
    }

    @Operation(summary = "Reject User")
    @PutMapping("/{id}/reject")
    public ResponseEntity<UserResponse> rejectUser(@PathVariable Long id) {
        log.info("[PUT] Received request to reject user with ID: {}", id);
        UserDto userDto = userService.rejectUser(id);
        UserResponse userResponse = UserResponse.fromDto(userDto);
        log.info("[PUT] Rejected user with ID: {}", id);
        return ResponseEntity.ok(userResponse);
    }
}
