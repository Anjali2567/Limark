package ai.leadplus.application.scrapejob;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.leadcompanyjob.LeadCompanyJobDto;
import ai.leadplus.application.leadcompanyjob.LeadCompanyJobFilePayload;
import ai.leadplus.application.leadcompanyjob.LeadCompanyJobService;
import ai.leadplus.domain.scrapejobs.ScrapeJob;
import ai.leadplus.domain.scrapejobs.ScrapeJobStatus;
import ai.leadplus.domain.scrapejobs.ScrapeSourceType;
import ai.leadplus.infrastructure.scraper.ScraperClient;
import ai.leadplus.infrastructure.scraper.ScraperProperties;
import ai.leadplus.infrastructure.scraper.dto.ScraperJobListResponse;
import ai.leadplus.infrastructure.scraper.dto.ScraperJobResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapeJobPollingService {

    private final ScraperProperties properties;
    private final ScraperClient scraperClient;
    private final ScrapeJobService scrapeJobService;
    private final LeadCompanyJobService leadCompanyJobService;
    private final ObjectMapper objectMapper;

    public void pollJobs() {
        LocalDateTime cursor = scrapeJobService.getPollingCursor(properties.getLookBackDays());
        log.info("Polling scraper jobs completed after {}", cursor);

        int processed = 0;
        ScraperJobListResponse response = scraperClient.getJobsAfter(cursor);
        if (response != null && !CollectionUtils.isEmpty(response.getJobs())) {
            for (ScraperJobResponse job : response.getJobs()) {
                try {
                    processJob(job);
                    processed++;
                } catch (Exception e) {
                    log.warn("Failed to process scraper job {}: {}", job.getId(), e.getMessage());
                }
            }
        }
        log.info("Scraper polling done — processed: {}", processed);
    }

    private void processJob(ScraperJobResponse job) {
        Optional<ScrapeJob> scrapeOpt = scrapeJobService.findByScraperJobId(String.valueOf(job.getId()));
        if (scrapeOpt.isEmpty()) {
            log.warn("No scrape job record found for scraper job {}, skipping", job.getId());
            return;
        }

        ScrapeJob scrapeJob = scrapeOpt.get();

        if (scrapeJob.getStatus().equals(ScrapeJobStatus.COMPLETED) || scrapeJob.getStatus().equals(ScrapeJobStatus.FAILED)) {
            return;
        }

        LocalDateTime completedAt = parseCompletedAt(job.getCompletedAt());

        if ("completed".equals(job.getStatus())) {
            if (scrapeJob.getSourceType().equals(ScrapeSourceType.COMPANY_JOB)) {
                handleCompanyJobCompleted(scrapeJob, job, completedAt);
            } else {
                handleCompleted(scrapeJob, job, completedAt);
            }
        } else if ("failed".equals(job.getStatus())) {
            scrapeJobService.markFailed(scrapeJob, completedAt);
        }
    }

    private void handleCompleted(ScrapeJob scrapeJob, ScraperJobResponse job, LocalDateTime completedAt) {
        Optional<Object> dataOpt = extractData(scrapeJob, job, completedAt);
        if (dataOpt.isEmpty()) return;

        LeadCompanyJobFilePayload.DataWrapper dataWrapper = objectMapper.convertValue(dataOpt.get(), LeadCompanyJobFilePayload.DataWrapper.class);
        List<LeadCompanyJobFilePayload.Careers> careers = CollectionUtils.isEmpty(dataWrapper.getCareers()) ? List.of() : dataWrapper.getCareers();

        List<LeadCompanyJobDto> jobs = careers.stream()
                .flatMap(c -> leadCompanyJobService.mapJobEntriesToDtos(c.getJobs()).stream())
                .toList();

        leadCompanyJobService.replaceJobsForCompany(scrapeJob.getCompanyId(), jobs);
        scrapeJobService.markCompleted(scrapeJob, completedAt);
    }

    private void handleCompanyJobCompleted(ScrapeJob scrapeJob, ScraperJobResponse job, LocalDateTime completedAt) {
        Optional<Object> dataOpt = extractData(scrapeJob, job, completedAt);
        if (dataOpt.isEmpty()) return;

        Map<String, Object> dataMap = objectMapper.convertValue(dataOpt.get(), new TypeReference<>() {
        });
        Object jobDetails = dataMap.get("job_details");
        if (jobDetails == null) {
            log.warn("Scraper job {} (COMPANY_JOB) has no job_details, marking FAILED", job.getId());
            scrapeJobService.markFailed(scrapeJob, completedAt);
            return;
        }

        JobDetailPayload payload = objectMapper.convertValue(jobDetails, JobDetailPayload.class);
        try {
            leadCompanyJobService.enrichJobWithDetails(scrapeJob.getSourceId(), payload);
        } catch (ResourceNotFoundException e) {
            log.warn("Job {} no longer active (replaced by website re-scrape), marking FAILED", scrapeJob.getSourceId());
            scrapeJobService.markFailed(scrapeJob, completedAt);
            return;
        }
        leadCompanyJobService.aggregateTechToCompany(scrapeJob.getCompanyId());
        scrapeJobService.markCompleted(scrapeJob, completedAt);

        log.info("Enriched job {} for company {} (scraper job {})", scrapeJob.getSourceId(), scrapeJob.getCompanyId(), job.getId());
    }

    private LocalDateTime parseCompletedAt(String completedAt) {
        if (completedAt == null) return LocalDateTime.now();
        try {
            return Instant.parse(completedAt).atZone(ZoneOffset.UTC).toLocalDateTime();
        } catch (Exception e) {
            log.warn("Could not parse completedAt '{}', using now", completedAt);
            return LocalDateTime.now();
        }
    }

    private Optional<Object> extractData(ScrapeJob scrapeJob, ScraperJobResponse job, LocalDateTime completedAt) {
        if (job.getResult() == null) {
            log.warn("Scraper job {} completed with no result, marking FAILED", job.getId());
            scrapeJobService.markFailed(scrapeJob, completedAt);
            return Optional.empty();
        }
        Object data = job.getResult().get("data");
        if (data == null) {
            log.warn("Scraper job {} has no data in result, marking FAILED", job.getId());
            scrapeJobService.markFailed(scrapeJob, completedAt);
            return Optional.empty();
        }
        return Optional.of(data);
    }
}
