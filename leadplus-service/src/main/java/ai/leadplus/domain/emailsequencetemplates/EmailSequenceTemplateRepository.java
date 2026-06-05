package ai.leadplus.domain.emailsequencetemplates;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailSequenceTemplateRepository extends JpaRepository<EmailSequenceTemplate, Long> {

    List<EmailSequenceTemplate> findAllByTenantId(Long tenantId);

    Optional<EmailSequenceTemplate> findByIdAndTenantId(Long id, Long tenantId);
}
