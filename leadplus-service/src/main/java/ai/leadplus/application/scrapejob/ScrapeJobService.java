package ai.leadplus.application.scrapejob;

import ai.leadplus.domain.scrapejobs.ScrapeJob;
import ai.leadplus.domain.scrapejobs.ScrapeJobRepository;
import ai.leadplus.domain.scrapejobs.ScrapeJobStatus;
import ai.leadplus.domain.scrapejobs.ScrapeSourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScrapeJobService {

    private final ScrapeJobRepository scrapeJobRepository;

    public ScrapeJob createPending(Long companyId, ScrapeSourceType sourceType, Long sourceId) {
        return scrapeJobRepository.save(ScrapeJob.builder()
                .companyId(companyId)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .status(ScrapeJobStatus.PENDING)
                .scheduledAt(LocalDateTime.now())
                .build());
    }

    public void confirmScheduled(ScrapeJob scrapeJob, String scraperJobId) {
        scrapeJob.setScraperJobId(scraperJobId);
        scrapeJobRepository.save(scrapeJob);
    }

    public void rollback(Long id) {
        scrapeJobRepository.deleteById(id);
    }

    public void markCompleted(ScrapeJob scrapeJob, LocalDateTime completedAt) {
        scrapeJob.setStatus(ScrapeJobStatus.COMPLETED);
        scrapeJob.setCompletedAt(completedAt);
        scrapeJobRepository.save(scrapeJob);
    }

    public void markFailed(ScrapeJob scrapeJob, LocalDateTime completedAt) {
        scrapeJob.setStatus(ScrapeJobStatus.FAILED);
        scrapeJob.setCompletedAt(completedAt);
        scrapeJobRepository.save(scrapeJob);
    }

    public Optional<ScrapeJob> findByScraperJobId(String scraperJobId) {
        return scrapeJobRepository.findByScraperJobId(scraperJobId);
    }

    public LocalDateTime getPollingCursor(int lookBackDays) {
        return scrapeJobRepository.findTopByCompletedAtNotNullOrderByCompletedAtDesc()
                .map(ScrapeJob::getCompletedAt)
                .orElseGet(() -> LocalDateTime.now().minusDays(lookBackDays));
    }

    public List<Long> getBusyCompanyIds(LocalDateTime cutoff) {
        return scrapeJobRepository
                .findBySourceTypeAndStatusInAndScheduledAtAfter(
                        ScrapeSourceType.COMPANY_WEBSITE,
                        List.of(ScrapeJobStatus.PENDING, ScrapeJobStatus.COMPLETED, ScrapeJobStatus.FAILED),
                        cutoff)
                .stream()
                .map(ScrapeJob::getCompanyId)
                .distinct()
                .toList();
    }

    public List<Long> getBusyJobIds(LocalDateTime cutoff) {
        return scrapeJobRepository
                .findBySourceTypeAndStatusInAndScheduledAtAfter(
                        ScrapeSourceType.COMPANY_JOB,
                        List.of(ScrapeJobStatus.PENDING, ScrapeJobStatus.COMPLETED, ScrapeJobStatus.FAILED),
                        cutoff)
                .stream()
                .map(ScrapeJob::getSourceId)
                .distinct()
                .toList();
    }
}
