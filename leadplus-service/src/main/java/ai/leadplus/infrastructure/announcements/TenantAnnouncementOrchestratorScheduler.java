package ai.leadplus.infrastructure.announcements;

import ai.leadplus.application.announcementorchestrator.TenantAnnouncementOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "announcement.orchestration.scheduler.enabled", havingValue = "true")
public class TenantAnnouncementOrchestratorScheduler {

    private final TenantAnnouncementOrchestratorService tenantAnnouncementOrchestratorService;

    @Scheduled(cron = "${announcement.orchestration.scheduler.cron}")
    public void run() {
        tenantAnnouncementOrchestratorService.processAnnouncements();
    }
}
