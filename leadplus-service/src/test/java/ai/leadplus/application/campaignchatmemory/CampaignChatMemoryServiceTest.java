package ai.leadplus.application.campaignchatmemory;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.campaignchatmemory.CampaignChatMemory;
import ai.leadplus.domain.campaignchatmemory.CampaignChatMemoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CampaignChatMemoryServiceTest {

    @Mock
    private CampaignChatMemoryRepository campaignChatMemoryRepository;

    @InjectMocks
    private CampaignChatMemoryService campaignChatMemoryService;

    @Test
    void shouldReturnDtoWhenCampaignFoundById() {
        CampaignChatMemory campaign = CampaignChatMemory.builder()
                .id(1L)
                .tenantId(1L)
                .workspaceId(6L)
                .build();

        when(campaignChatMemoryRepository.findById(1L)).thenReturn(Optional.of(campaign));

        CampaignChatMemoryDto dto = campaignChatMemoryService.getCampaignChatMemoryById(1L);

        assertNotNull(dto);
        verify(campaignChatMemoryRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenNotFoundById() {
        when(campaignChatMemoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> campaignChatMemoryService.getCampaignChatMemoryById(999L));
        verify(campaignChatMemoryRepository).findById(999L);
    }

    @Test
    void shouldCreateBasicCampaignChatMemory() {
        when(campaignChatMemoryRepository.save(any(CampaignChatMemory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CampaignChatMemoryDto dto = campaignChatMemoryService.createBasicCampaignChatMemory(2L, 7L);

        assertNotNull(dto);

        ArgumentCaptor<CampaignChatMemory> captor = ArgumentCaptor.forClass(CampaignChatMemory.class);
        verify(campaignChatMemoryRepository).save(captor.capture());

        CampaignChatMemory saved = captor.getValue();
        assertEquals(2L, saved.getTenantId());
        assertEquals(7L, saved.getWorkspaceId());
    }

    @Test
    void shouldUpdateCampaignChatMemory() {
        CampaignChatMemory existing = CampaignChatMemory.builder()
                .id(2L)
                .tenantId(3L)
                .workspaceId(8L)
                .build();

        CampaignChatMemoryDto dto = CampaignChatMemoryDto.fromEntity(existing);

        when(campaignChatMemoryRepository.save(any(CampaignChatMemory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CampaignChatMemoryDto result = campaignChatMemoryService.updateCampaignChatMemory(dto);

        assertNotNull(result);

        ArgumentCaptor<CampaignChatMemory> captor = ArgumentCaptor.forClass(CampaignChatMemory.class);
        verify(campaignChatMemoryRepository).save(captor.capture());

        CampaignChatMemory saved = captor.getValue();
        assertEquals(existing.getTenantId(), saved.getTenantId());
        assertEquals(existing.getWorkspaceId(), saved.getWorkspaceId());
    }

    @Test
    void shouldFindByConversationId() {
        CampaignChatMemory campaign = CampaignChatMemory.builder()
                .id(3L)
                .tenantId(4L)
                .workspaceId(5L)
                .build();

        when(campaignChatMemoryRepository.findByWorkspaceIdAndTargetingCriteria_ConversationId(5L, "conv123"))
                .thenReturn(Optional.of(campaign));

        CampaignChatMemoryDto dto = campaignChatMemoryService.findCampaignMemoryByConversationId(5L, "conv123");

        assertNotNull(dto);
        verify(campaignChatMemoryRepository).findByWorkspaceIdAndTargetingCriteria_ConversationId(5L, "conv123");
    }

    @Test
    void shouldThrowWhenConversationIdNotFound() {
        when(campaignChatMemoryRepository.findByWorkspaceIdAndTargetingCriteria_ConversationId(5L, "missing"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> campaignChatMemoryService.findCampaignMemoryByConversationId(5L, "missing"));

        verify(campaignChatMemoryRepository).findByWorkspaceIdAndTargetingCriteria_ConversationId(5L, "missing");
    }
}
