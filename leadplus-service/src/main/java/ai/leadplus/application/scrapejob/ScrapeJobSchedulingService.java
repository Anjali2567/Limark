package ai.leadplus.application.scrapejob;

import ai.leadplus.application.leadcompany.LeadCompanyDto;
import ai.leadplus.application.leadcompany.LeadCompanyService;
import ai.leadplus.domain.scrapejobs.ScrapeJob;
import ai.leadplus.domain.scrapejobs.ScrapeSourceType;
import ai.leadplus.infrastructure.scraper.ScraperClient;
import ai.leadplus.infrastructure.scraper.ScraperCompanyJobTemplate;
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
public class ScrapeJobSchedulingService {

    private final ScraperProperties properties;
    private final ScraperClient scraperClient;
    private final ScraperCompanyJobTemplate companyJobTemplate;
    private final ScrapeJobService scrapeJobService;
    private final LeadCompanyService leadCompanyService;

    public void scheduleJobs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(properties.getLookBackDays());
        List<Long> busyCompanyIds = scrapeJobService.getBusyCompanyIds(cutoff);
        List<LeadCompanyDto> companies = leadCompanyService.findCompaniesNeedingScrape(busyCompanyIds, properties.getJobsPerHour());

        if (CollectionUtils.isEmpty(companies)) {
            log.info("No companies need scrape scheduling");
            return;
        }

        int scheduled = 0;

        for (LeadCompanyDto company : companies) {
            ScrapeJob scrapeJob = scrapeJobService.createPending(company.getId(), ScrapeSourceType.COMPANY_WEBSITE, company.getId());

            try {
                ScraperJobResponse response = scraperClient.createJob(CreateScraperJobRequest.builder()
                        .url(company.getWebsiteUrl())
                        .externalId(String.valueOf(scrapeJob.getId()))
                        .options(companyJobTemplate.getOptions())
                        .actions(companyJobTemplate.getActions())
                        .build());

                scrapeJobService.confirmScheduled(scrapeJob, String.valueOf(response.getId()));
                scheduled++;
            } catch (Exception e) {
                rollbackAndWarn(String.valueOf(company.getId()), String.valueOf(scrapeJob.getId()), e);
            }
        }

        log.info("Scheduled {} scrape jobs", scheduled);
    }

    public void scheduleJobForCompany(String idOrDomain) {
        LeadCompanyDto company = leadCompanyService.getCompanyByIdOrDomain(idOrDomain);
        ScrapeJob scrapeJob = scrapeJobService.createPending(company.getId(), ScrapeSourceType.COMPANY_WEBSITE, company.getId());

        try {
            ScraperJobResponse response = scraperClient.createJob(CreateScraperJobRequest.builder()
                    .url(company.getWebsiteUrl())
                    .externalId(String.valueOf(scrapeJob.getId()))
                    .options(companyJobTemplate.getOptions())
                    .actions(companyJobTemplate.getActions())
                    .build());
            scrapeJobService.confirmScheduled(scrapeJob, String.valueOf(response.getId()));
            log.info("Scheduled scrape job for company {}", company.getId());
        } catch (Exception e) {
            rollbackAndWarn(String.valueOf(company.getId()), String.valueOf(scrapeJob.getId()), e);
            throw e;
        }
    }

    private void rollbackAndWarn(String companyId, String scrapeJobId, Exception e) {
        log.warn("Failed to schedule scrape job for company {}, rolling back: {}", companyId, e.getMessage());
        scrapeJobService.rollback(Long.valueOf(scrapeJobId));
    }
}
