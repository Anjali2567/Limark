package ai.leadplus.infrastructure.google.gmail;

import ai.leadplus.application.campaigncontacts.GmailReplySyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "google.scheduler.enabled", havingValue = "true")
public class GmailReplySyncScheduler {

    private final GmailReplySyncService gmailReplySyncService;

    @Scheduled(cron = "${google.scheduler.cron}")
    public void syncGmailReplies() {
        log.info("Starting daily Gmail reply sync job");
        gmailReplySyncService.syncReplies();
    }
}

