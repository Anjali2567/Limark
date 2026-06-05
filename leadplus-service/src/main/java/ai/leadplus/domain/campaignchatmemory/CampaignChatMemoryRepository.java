package ai.leadplus.domain.campaignchatmemory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampaignChatMemoryRepository extends JpaRepository<CampaignChatMemory, Long> {

    List<CampaignChatMemory> findByWorkspaceId(Long workspaceId);

    default Optional<CampaignChatMemory> findByWorkspaceIdAndTargetingCriteria_ConversationId(Long workspaceId, String conversationId) {
        return findByWorkspaceId(workspaceId).stream()
            .filter(c -> c.getTargetingCriteria() != null && 
                        conversationId.equals(c.getTargetingCriteria().getConversationId()))
            .findFirst();
    }
}
