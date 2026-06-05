package ai.leadplus.application.campaignchatmemory;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.campaignchatmemory.CampaignChatMemory;
import ai.leadplus.domain.campaignchatmemory.CampaignChatMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CampaignChatMemoryService {

    private final CampaignChatMemoryRepository campaignChatMemoryRepository;

    public CampaignChatMemoryDto getCampaignChatMemoryById(Long id) {
        CampaignChatMemory campaign = findCampaignMemoryById(id);
        return CampaignChatMemoryDto.fromEntity(campaign);
    }

    private CampaignChatMemory findCampaignMemoryById(Long id) {
        return campaignChatMemoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
    }

    public CampaignChatMemoryDto createBasicCampaignChatMemory(Long tenantId, Long workspaceId) {
        CampaignChatMemory campaign = CampaignChatMemory.builder()
                .tenantId(tenantId)
                .workspaceId(workspaceId)
                .build();
        campaignChatMemoryRepository.save(campaign);
        return CampaignChatMemoryDto.fromEntity(campaign);
    }

    public CampaignChatMemoryDto updateCampaignChatMemory(CampaignChatMemoryDto campaignChatMemoryDto) {
        CampaignChatMemory campaignChatMemory = campaignChatMemoryDto.toEntity();
        campaignChatMemoryRepository.save(campaignChatMemory);
        return CampaignChatMemoryDto.fromEntity(campaignChatMemory);
    }

    public CampaignChatMemoryDto findCampaignMemoryByConversationId(Long workspaceId, String conversationId) {
        CampaignChatMemory campaign = campaignChatMemoryRepository.findByWorkspaceIdAndTargetingCriteria_ConversationId(workspaceId, conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign Chat memory not found with conversationId: " + conversationId));
        return CampaignChatMemoryDto.fromEntity(campaign);
    }
}
