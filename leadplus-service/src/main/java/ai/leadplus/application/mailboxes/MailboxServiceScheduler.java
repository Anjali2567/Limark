package ai.leadplus.application.mailboxes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mailbox.scheduler.enabled", havingValue = "true")
public class MailboxServiceScheduler {

    private final MailboxService mailboxService;

    @Scheduled(cron = "${mailbox.scheduler.cron}")
    public void resetMailboxEmailCount() {
        long count = mailboxService.resetMailboxEmailCountDaily();
        log.info("Reset emails sent today count for {} mailboxes", count);
    }
}
