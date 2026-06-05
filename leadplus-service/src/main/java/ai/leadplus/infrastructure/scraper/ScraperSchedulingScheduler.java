package ai.leadplus.infrastructure.scraper;

import ai.leadplus.application.scrapejob.ScrapeJobSchedulingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scraper.scheduler.scheduling.enabled", havingValue = "true")
public class ScraperSchedulingScheduler {

    private final ScrapeJobSchedulingService schedulingService;

    @Scheduled(cron = "${scraper.scheduler.scheduling.cron}")
    public void run() {
        log.info("Running scraper scheduling job");
        schedulingService.scheduleJobs();
    }
}
