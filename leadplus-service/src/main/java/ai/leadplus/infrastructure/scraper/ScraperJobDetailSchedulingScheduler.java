package ai.leadplus.infrastructure.scraper;

import ai.leadplus.application.scrapejob.ScrapeJobDetailSchedulingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scraper.scheduler.job-detail.enabled", havingValue = "true")
public class ScraperJobDetailSchedulingScheduler {

    private final ScrapeJobDetailSchedulingService schedulingService;

    @Scheduled(cron = "${scraper.scheduler.job-detail.cron}")
    public void run() {
        log.info("Running job detail scrape scheduling");
        schedulingService.scheduleJobDetailScrapes();
    }
}
