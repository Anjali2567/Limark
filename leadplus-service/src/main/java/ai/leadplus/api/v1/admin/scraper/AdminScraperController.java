package ai.leadplus.api.v1.admin.scraper;

import ai.leadplus.application.scrapejob.ScrapeJobDetailSchedulingService;
import ai.leadplus.application.scrapejob.ScrapeJobPollingService;
import ai.leadplus.application.scrapejob.ScrapeJobSchedulingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/admin/scraper")
@RequiredArgsConstructor
@Tag(name = "Scraper", description = "Scraper trigger APIs")
public class AdminScraperController {

    private final ScrapeJobSchedulingService schedulingService;
    private final ScrapeJobPollingService pollingService;
    private final ScrapeJobDetailSchedulingService jobDetailSchedulingService;

    @Operation(summary = "Trigger scrape scheduling", description = "Finds companies needing a scrape and schedules jobs with the scraper service")
    @PostMapping("/schedule")
    public ResponseEntity<Void> schedule() {
        log.info("[POST] Manual scrape scheduling triggered");
        schedulingService.scheduleJobs();
        log.info("[POST] Manual scrape scheduling completed");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Trigger scrape polling", description = "Polls the scraper service for completed jobs and updates company job data")
    @PostMapping("/poll")
    public ResponseEntity<Void> poll() {
        log.info("[POST] Manual scrape polling triggered");
        pollingService.pollJobs();
        log.info("[POST] Manual scrape polling completed");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Trigger job detail scrape scheduling", description = "Schedules job detail scrapes for all eligible jobs across all companies")
    @PostMapping("/schedule-job-details")
    public ResponseEntity<Void> scheduleJobDetails() {
        log.info("[POST] Manual job detail scheduling triggered");
        jobDetailSchedulingService.scheduleJobDetailScrapes();
        log.info("[POST] Manual job detail scheduling completed");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Trigger scrape scheduling for a company", description = "Schedules a company website scrape job for the specified company")
    @PostMapping("/schedule/{companyIdOrDomain}")
    public ResponseEntity<Void> scheduleForCompany(@PathVariable String companyIdOrDomain) {
        log.info("[POST] Manual scrape scheduling triggered for company {}", companyIdOrDomain);
        schedulingService.scheduleJobForCompany(companyIdOrDomain);
        log.info("[POST] Manual scrape scheduling completed for company {}", companyIdOrDomain);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Trigger job detail scrape scheduling for a company", description = "Schedules job detail scrapes for all eligible jobs belonging to the specified company")
    @PostMapping("/schedule-job-details/{companyIdOrDomain}")
    public ResponseEntity<Void> scheduleJobDetailsForCompany(@PathVariable String companyIdOrDomain) {
        log.info("[POST] Manual job detail scheduling triggered for company {}", companyIdOrDomain);
        jobDetailSchedulingService.scheduleJobDetailScrapesForCompany(companyIdOrDomain);
        log.info("[POST] Manual job detail scheduling completed for company {}", companyIdOrDomain);
        return ResponseEntity.noContent().build();
    }
}
