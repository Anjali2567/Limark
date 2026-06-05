package ai.leadplus.domain.sequencetemplates;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SequenceTemplateRepository extends JpaRepository<SequenceTemplate, Long> {
}
