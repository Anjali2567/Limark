package ai.leadplus.domain.scrapejobs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScrapeJobRepository extends JpaRepository<ScrapeJob, Long> {

    Optional<ScrapeJob> findByScraperJobId(String scraperJobId);

    Optional<ScrapeJob> findTopByCompletedAtNotNullOrderByCompletedAtDesc();

    List<ScrapeJob> findBySourceTypeAndStatusInAndScheduledAtAfter(ScrapeSourceType sourceType, List<ScrapeJobStatus> statuses, LocalDateTime cutoff);
}
