package ai.leadplus.domain.leadcompanyjobs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadCompanyJobRepository extends JpaRepository<LeadCompanyJob, Long> {

    Optional<LeadCompanyJob> findByIdAndActiveTrue(Long id);

    Optional<LeadCompanyJob> findByIdAndLeadCompanyIdAndActiveTrue(Long id, Long leadCompanyId);

    List<LeadCompanyJob> findAllByLeadCompanyIdAndActiveTrue(Long leadCompanyId);

    Page<LeadCompanyJob> findAllByLeadCompanyIdAndActiveTrue(Long leadCompanyId, Pageable pageable);

    List<LeadCompanyJob> findByActiveTrueAndJobUrlIsNotNullAndIdNotIn(List<Long> ids, Pageable pageable);

    List<LeadCompanyJob> findByLeadCompanyIdAndActiveTrueAndJobUrlIsNotNullAndIdNotIn(Long leadCompanyId, List<Long> ids);
}
