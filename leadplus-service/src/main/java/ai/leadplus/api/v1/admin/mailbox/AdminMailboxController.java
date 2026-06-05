package ai.leadplus.api.v1.admin.mailbox;

import ai.leadplus.application.campaigncontacts.AzureReplySyncService;
import ai.leadplus.application.campaigncontacts.GmailReplySyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/admin/mailbox")
@RequiredArgsConstructor
@Tag(name = "Mailbox", description = "Mailbox Sync APIs")
public class AdminMailboxController {

    private final GmailReplySyncService gmailReplySyncService;
    private final AzureReplySyncService azureReplySyncService;

    @Operation(summary = "Sync Google Mailbox Replies")
    @PostMapping("/google/replies")
    public ResponseEntity<Void> triggerGmailReplySync() {
        log.info("Starting manual Gmail reply sync job");
        gmailReplySyncService.syncReplies();
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Sync Outlook Mailbox Replies")
    @PostMapping("/outlook/replies")
    public ResponseEntity<Void> triggerOutlookReplySync() {
        log.info("Starting manual Outlook reply sync job");
        azureReplySyncService.syncReplies();
        return ResponseEntity.accepted().build();
    }
}
