package ai.leadplus.api.v1.contactemails;

import ai.leadplus.api.common.UserValidator;
import ai.leadplus.application.contactemails.ContactEmailAiService;
import ai.leadplus.application.contactemails.ContactEmailDto;
import ai.leadplus.application.contactemails.ContactEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/tenants/{tenantId}/workspaces/{workspaceId}/leads/contacts")
@RequiredArgsConstructor
@Tag(name = "Contact Email API", description = "Endpoints for sending emails to contacts")
public class ContactEmailController {

    private final ContactEmailService contactEmailService;
    private final ContactEmailAiService contactEmailAiService;
    private final UserValidator userValidator;

    @Operation(summary = "Send Email to Contact", description = "Sends an email to a contact using the specified mailbox")
    @PostMapping("/{contactId}/emails")
    public ResponseEntity<Void> sendEmailToContact(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @PathVariable Long contactId,
            @RequestBody @Valid ContactEmailRequest emailRequest
    ) {
        Long userId = Long.parseLong(userValidator.getAuthenticatedUserId());
        log.info("[POST] Request received to send email to contactId={} using mailboxId={}", contactId, emailRequest.getMailboxId());
        ContactEmailDto contactEmailDto = emailRequest.toDto(tenantId, workspaceId, contactId);
        contactEmailService.sendEmail(Long.parseLong(emailRequest.getMailboxId()), userId, contactEmailDto);
        log.info("[POST] Successfully sent email to contactId={} using mailboxId={}", contactId, emailRequest.getMailboxId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Generate Email for Contact", description = "Uses AI to generate an email subject and body based on the provided prompt")
    @PostMapping("/emails/generate")
    public ResponseEntity<ContactEmailGenerateResponse> generateEmailForContact(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @RequestBody @Valid ContactEmailGenerateRequest generateRequest
    ) {
        log.info("[POST] Request received to generate email (tenantId={}, workspaceId={})", tenantId, workspaceId);
        ContactEmailGenerateResponse response = ContactEmailGenerateResponse.fromCompletion(
                contactEmailAiService.generateEmail(generateRequest.getPrompt())
        );
        log.info("[POST] Successfully generated email (tenantId={}, workspaceId={})", tenantId, workspaceId);
        return ResponseEntity.ok(response);
    }
}
