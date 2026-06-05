package ai.leadplus.api.v1.mailboxes;

import ai.leadplus.api.common.UserValidator;
import ai.leadplus.api.v1.auth.IdentityAuthRequest;
import ai.leadplus.application.auth.AuthenticationService;
import ai.leadplus.application.mailboxes.MailboxDto;
import ai.leadplus.application.mailboxes.MailboxService;
import ai.leadplus.application.users.UserDto;
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
@RequestMapping("/v1/tenants/{tenantId}/workspaces/{workspaceId}/mailboxes")
@RequiredArgsConstructor
@Tag(name = "Mailbox API", description = "APIs for Mailbox Integration")
public class MailboxController {

    private final MailboxService mailboxService;
    private final AuthenticationService authenticationService;
    private final UserValidator userValidator;

    @Operation(summary = "Get Connected Mailbox Details")
    @GetMapping
    public ResponseEntity<List<MailboxResponse>> getConnectedMailboxes(@PathVariable Long tenantId,
                                                                       @PathVariable Long workspaceId) {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[GET] Received request to fetch connected mailboxes for UserId: {} and WorkspaceId: {}", userId, workspaceId);
        List<MailboxDto> mailboxDtoList = mailboxService.getConnectedMailboxes(workspaceId, Long.parseLong(userId));
        List<MailboxResponse> responses = mailboxDtoList.stream()
                .map(MailboxResponse::fromDto)
                .toList();
        log.info("[GET] Fetched {} connected mailboxes for UserId: {} and WorkspaceId: {}", responses.size(), userId, workspaceId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get Authorized Mailbox Details")
    @GetMapping("/authorized")
    public ResponseEntity<List<MailboxResponse>> getAuthorizedMailboxes(@PathVariable Long tenantId,
                                                                       @PathVariable Long workspaceId) {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[GET] Received request to fetch authorized mailboxes for UserId: {} and WorkspaceId: {}", userId, workspaceId);
        List<MailboxDto> mailboxDtoList = mailboxService.getAuthorizedMailboxes(workspaceId, Long.parseLong(userId));
        List<MailboxResponse> responses = mailboxDtoList.stream()
                .map(MailboxResponse::fromDto)
                .toList();
        log.info("[GET] Fetched {} authorized mailboxes for UserId: {} and WorkspaceId: {}", responses.size(), userId, workspaceId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Connect Outlook MailBox")
    @PostMapping("/microsoft")
    public ResponseEntity<Void> authenticateMicrosoftOAuth(@PathVariable Long tenantId,
                                                           @PathVariable Long workspaceId,
                                                           @RequestBody IdentityAuthRequest request) {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[POST] Received request to authenticate via Microsoft OAuth for UserId: {} and WorkspaceId: {}", userId, workspaceId);
        authenticationService.authenticateMicrosoft(Long.parseLong(userId), workspaceId, request.getCode(), request.getRedirectUri());
        log.info("[POST] Authenticated via Microsoft OAuth for UserId: {} and WorkspaceId: {}", userId, workspaceId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Connect Gmail MailBox")
    @PostMapping("/google")
    public ResponseEntity<Void> authenticateGoogleOAuth(@PathVariable Long tenantId,
                                                           @PathVariable Long workspaceId,
                                                           @RequestBody IdentityAuthRequest request) {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[POST] Received request to authenticate via Google OAuth for UserId: {} and WorkspaceId: {}", userId, workspaceId);
        authenticationService.authenticateGoogle(Long.parseLong(userId), workspaceId, request.getCode(), request.getRedirectUri());
        log.info("[POST] Authenticated via Google OAuth for UserId: {} and WorkspaceId: {}", userId, workspaceId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Disconnect Mailbox")
    @DeleteMapping("/{mailboxId}")
    public ResponseEntity<Void> disconnectMailbox(@PathVariable Long tenantId,
                                                  @PathVariable Long workspaceId,
                                                  @PathVariable Long mailboxId) {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[DELETE] Received request to disconnect mailbox with Id: {} in WorkspaceId: {}", mailboxId, workspaceId);
        mailboxService.deactivateMailbox(mailboxId, workspaceId, Long.parseLong(userId));
        log.info("[DELETE] Disconnected mailbox with Id: {} in WorkspaceId: {}", mailboxId, workspaceId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Connect AWS SES To MailBox")
    @PostMapping("/aws-ses")
    public ResponseEntity<MailboxResponse> authenticateLeadAgentViaAwsSES(@PathVariable Long tenantId,
                                                                          @PathVariable Long workspaceId,
                                                                          @Valid @RequestBody MailboxSesRequest mailboxSesRequest) {
        UserDto userDto = userValidator.getAuthenticatedUser();
        log.info("[POST] Received request to authenticate via SES for UserId: {} and WorkspaceId: {}", userDto.getId(), workspaceId);
        MailboxDto mailboxDto = authenticationService.sendAwsSESVerificationEmail(userDto, workspaceId, mailboxSesRequest.getEmail());
        MailboxResponse response = MailboxResponse.fromDto(mailboxDto);
        log.info("[POST] Authenticated via SES for UserId: {} and WorkspaceId: {}", userDto.getId(), workspaceId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Connect Smtp To MailBox")
    @PostMapping("/smtp")
    public ResponseEntity<MailboxResponse> createSmtpMailbox(@PathVariable Long tenantId,
                                                             @PathVariable Long workspaceId,
                                                             @RequestBody @Valid MailboxSmtpRequest request) {
        UserDto userDto = userValidator.getAuthenticatedUser();
        log.info("[POST] Received request to create SMTP mailbox for UserId: {} and WorkspaceId: {}", userDto.getId(), workspaceId);
        MailboxDto mailboxDto  = mailboxService.createSmtpMailbox(userDto, workspaceId, request.getEmail(), request.getAppPassword());
        MailboxResponse response = MailboxResponse.fromDto(mailboxDto);
        log.info("[POST] Created SMTP mailbox for UserId: {} and WorkspaceId: {}", userDto.getId(), workspaceId);
        return ResponseEntity.ok(response);
    }
}
