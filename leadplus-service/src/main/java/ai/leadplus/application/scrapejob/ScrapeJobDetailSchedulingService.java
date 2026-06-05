package ai.leadplus.application.scrapejob;

import ai.leadplus.application.leadcompany.LeadCompanyDto;
import ai.leadplus.application.leadcompany.LeadCompanyService;
import ai.leadplus.application.leadcompanyjob.LeadCompanyJobService;
import ai.leadplus.domain.leadcompanyjobs.LeadCompanyJob;
import ai.leadplus.domain.scrapejobs.ScrapeJob;
import ai.leadplus.domain.scrapejobs.ScrapeSourceType;
import ai.leadplus.infrastructure.scraper.ScraperClient;
import ai.leadplus.infrastructure.scraper.ScraperJobDetailTemplate;
import ai.leadplus.infrastructure.scraper.ScraperProperties;
import ai.leadplus.infrastructure.scraper.dto.CreateScraperJobRequest;
import ai.leadplus.infrastructure.scraper.dto.ScraperJobResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapeJobDetailSchedulingService {

    private final ScraperProperties properties;
    private final ScraperClient scraperClient;
    private final ScraperJobDetailTemplate jobDetailTemplate;
    private final ScrapeJobService scrapeJobService;
    private final LeadCompanyJobService leadCompanyJobService;
    private final LeadCompanyService leadCompanyService;

    public void scheduleJobDetailScrapes() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(properties.getLookBackDays());
        List<Long> busyJobIds = scrapeJobService.getBusyJobIds(cutoff);
        List<LeadCompanyJob> jobs = leadCompanyJobService.findJobsNeedingDetailScrape(busyJobIds, properties.getJobDetailJobsPerRun());

        if (CollectionUtils.isEmpty(jobs)) {
            return;
        }

        int scheduled = scheduleAll(jobs);
        log.info("Scheduled {} job detail scrape jobs", scheduled);
    }

    public void scheduleJobDetailScrapesForCompany(String companyIdOrDomain) {
        LeadCompanyDto company = leadCompanyService.getCompanyByIdOrDomain(companyIdOrDomain);
        LocalDateTime cutoff = LocalDateTime.now().minusDays(properties.getLookBackDays());
        List<Long> busyJobIds = scrapeJobService.getBusyJobIds(cutoff);
        List<LeadCompanyJob> jobs = leadCompanyJobService.findJobsNeedingDetailScrapeForCompany(company.getId(), busyJobIds);

        if (CollectionUtils.isEmpty(jobs)) {
            return;
        }

        int scheduled = scheduleAll(jobs);
        log.info("Scheduled {} job detail scrape jobs for company {}", scheduled, company.getId());
    }

    private int scheduleAll(List<LeadCompanyJob> jobs) {
        int scheduled = 0;
        for (LeadCompanyJob job : jobs) {
            ScrapeJob scrapeJob = scrapeJobService.createPending(job.getLeadCompanyId(), ScrapeSourceType.COMPANY_JOB, job.getId());
            try {
                ScraperJobResponse response = scraperClient.createJob(CreateScraperJobRequest.builder()
                        .url(job.getJobUrl())
                        .externalId(String.valueOf(scrapeJob.getId()))
                        .options(jobDetailTemplate.getOptions())
                        .actions(jobDetailTemplate.getActions())
                        .build());
                scrapeJobService.confirmScheduled(scrapeJob, String.valueOf(response.getId()));
                scheduled++;
            } catch (Exception e) {
                log.warn("Failed to schedule job detail scrape for job {}, rolling back: {}", job.getId(), e.getMessage());
                scrapeJobService.rollback(scrapeJob.getId());
            }
        }
        return scheduled;
    }
}
