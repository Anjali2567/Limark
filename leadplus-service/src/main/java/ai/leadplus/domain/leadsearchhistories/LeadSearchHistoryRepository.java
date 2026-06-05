package ai.leadplus.domain.leadsearchhistories;

import ai.leadplus.domain.common.LeadType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadSearchHistoryRepository extends JpaRepository<LeadSearchHistory, Long> {

    List<LeadSearchHistory> findByUserIdAndType(Long userId, LeadType type);
}
