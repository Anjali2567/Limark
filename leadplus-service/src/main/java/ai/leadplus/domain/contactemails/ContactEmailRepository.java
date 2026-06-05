package ai.leadplus.domain.contactemails;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContactEmailRepository extends JpaRepository<ContactEmail, Long> {

    long countByTenantId(Long tenantId);

    Optional<ContactEmail> findTopByTenantIdOrderByCreatedAtDesc(Long tenantId);
}
