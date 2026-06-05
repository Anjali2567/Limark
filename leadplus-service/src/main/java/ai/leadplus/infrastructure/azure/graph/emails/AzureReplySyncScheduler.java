package ai.leadplus.infrastructure.azure.graph.emails;

import ai.leadplus.application.campaigncontacts.AzureReplySyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "azure.scheduler.enabled", havingValue = "true")
public class AzureReplySyncScheduler {

    private final AzureReplySyncService azureReplySyncService;

    @Scheduled(cron = "${azure.scheduler.cron}")
    public void syncOutlookReplies() {
        log.info("Starting daily Outlook reply sync job");
        azureReplySyncService.syncReplies();
        log.info("Outlook reply sync completed");
    }
}