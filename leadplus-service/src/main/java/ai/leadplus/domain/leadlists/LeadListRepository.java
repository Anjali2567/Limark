package ai.leadplus.domain.leadlists;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface LeadListRepository extends JpaRepository<LeadList, Long>, JpaSpecificationExecutor<LeadList> {

    Optional<LeadList> findByIdAndTenantIdAndWorkspaceIdAndActiveTrue(Long id, Long tenantId, Long workspaceId);
}
