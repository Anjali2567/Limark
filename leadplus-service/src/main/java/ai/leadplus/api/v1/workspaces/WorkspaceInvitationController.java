package ai.leadplus.api.v1.workspaces;

import ai.leadplus.application.workspaceuser.WorkspaceUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/workspaces/invitation")
@RequiredArgsConstructor
@Tag(name = "Workspace Invitation", description = "APIs for managing workspace invitation")
public class WorkspaceInvitationController {

    private final WorkspaceUserService workspaceUserService;

    @Operation(summary = "Accept Workspace Invitation")
    @PostMapping("/accept")
    public ResponseEntity<Void> acceptInvitation(@RequestParam String token) {
        workspaceUserService.acceptInvitation(token);
        return ResponseEntity.ok().build();
    }
}
