package ai.leadplus.infrastructure.scraper;

import ai.leadplus.application.scrapejob.ScrapeJobPollingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scraper.scheduler.polling.enabled", havingValue = "true")
public class ScraperPollingScheduler {

    private final ScrapeJobPollingService pollingService;

    @Scheduled(cron = "${scraper.scheduler.polling.cron}")
    public void run() {
        log.info("Running scraper polling job");
        pollingService.pollJobs();
    }
}
