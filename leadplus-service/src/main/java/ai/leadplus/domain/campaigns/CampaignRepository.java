package ai.leadplus.domain.campaigns;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long>, JpaSpecificationExecutor<Campaign> {

    default Optional<Campaign> findByWorkspaceIdAndTargetingCriteria_ConversationId(Long workspaceId, String conversationId) {
        return findAllByWorkspaceId(workspaceId).stream()
            .filter(c -> c.getTargetingCriteria() != null && 
                        conversationId.equals(c.getTargetingCriteria().getConversationId()))
            .findFirst();
    }

    List<Campaign> findAllByWorkspaceId(Long workspaceId);

    Page<Campaign> findAllByWorkspaceId(Long workspaceId, Pageable pageable);

    boolean existsBySendingMailboxIdAndStatusIn(Long sendingMailboxId, List<CampaignStatus> campaignStatuses);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndStatusIn(Long tenantId, List<CampaignStatus> statuses);

    @Query("SELECT new map(c.id as id) FROM Campaign c WHERE c.tenantId = ?1")
    List<CampaignIdDto> findIdsByTenantId(Long tenantId);
}
