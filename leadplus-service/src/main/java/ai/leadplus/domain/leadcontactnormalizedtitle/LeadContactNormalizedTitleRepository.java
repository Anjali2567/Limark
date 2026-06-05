package ai.leadplus.domain.leadcontactnormalizedtitle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeadContactNormalizedTitleRepository extends JpaRepository<LeadContactNormalizedTitle, Long> {
    Optional<LeadContactNormalizedTitle> findByLeadContactId(Long leadContactId);
}