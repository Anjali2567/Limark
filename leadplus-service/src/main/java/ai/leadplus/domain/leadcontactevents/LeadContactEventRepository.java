package ai.leadplus.domain.leadcontactevents;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadContactEventRepository extends JpaRepository<LeadContactEvent, Long> {

    Page<LeadContactEvent> findAllByTenantIdAndWorkspaceIdAndContactId(Long tenantId, Long workspaceId, Long contactId, Pageable pageable);

    Page<LeadContactEvent> findAllByTenantIdAndWorkspaceIdAndContactIdAndCategory(Long tenantId, Long workspaceId, Long contactId, LeadContactEventCategory category, Pageable pageable);

    long countByTenantIdAndWorkspaceIdAndContactIdAndCategory(Long tenantId, Long workspaceId, Long contactId, LeadContactEventCategory category);

    long countByTenantIdAndContactIdAndCategory(Long tenantId, Long contactId, LeadContactEventCategory category);

    Page<LeadContactEvent> findAllByTenantIdAndContactId(Long tenantId, Long contactId, Pageable pageable);

    Page<LeadContactEvent> findAllByTenantIdAndContactIdAndCategory(Long tenantId, Long contactId, LeadContactEventCategory category, Pageable pageable);
}
