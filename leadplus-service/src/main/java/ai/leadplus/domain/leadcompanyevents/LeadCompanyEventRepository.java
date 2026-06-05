package ai.leadplus.domain.leadcompanyevents;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadCompanyEventRepository extends JpaRepository<LeadCompanyEvent, Long> {

    Optional<LeadCompanyEvent> findByIdAndLeadCompanyIdAndActiveTrue(Long id, Long companyId);

    List<LeadCompanyEvent> findAllByLeadCompanyIdAndActiveTrue(Long companyId);


}
