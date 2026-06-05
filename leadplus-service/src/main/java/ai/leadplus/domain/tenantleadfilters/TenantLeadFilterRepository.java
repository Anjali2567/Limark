package ai.leadplus.domain.tenantleadfilters;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantLeadFilterRepository extends JpaRepository<TenantLeadFilter, Long> {

    Optional<TenantLeadFilter> findByIdAndTenantIdAndActiveTrue(Long id, Long tenantId);

    List<TenantLeadFilter> findByTenantIdAndActiveTrue(Long tenantId);
}