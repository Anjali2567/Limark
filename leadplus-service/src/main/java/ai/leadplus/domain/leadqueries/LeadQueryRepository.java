package ai.leadplus.domain.leadqueries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface LeadQueryRepository extends JpaRepository<LeadQuery, Long>, JpaSpecificationExecutor<LeadQuery> {

    boolean existsByTypeAndValueIgnoreCase(LeadQueryType leadQueryType, String value);

    List<LeadQuery> findAllByType(LeadQueryType type);
}
