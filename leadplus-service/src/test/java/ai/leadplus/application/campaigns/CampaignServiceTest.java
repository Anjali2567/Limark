package ai.leadplus.application.campaigns;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.mailboxes.MailboxDto;
import ai.leadplus.application.mailboxes.MailboxService;
import ai.leadplus.domain.campaigns.Campaign;
import ai.leadplus.domain.campaigns.CampaignRepository;
import ai.leadplus.domain.campaigns.CampaignStatus;
import ai.leadplus.domain.mailboxes.MailBoxType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private MailboxService mailboxService;

    @Mock
    private CampaignSearchService campaignSearchService;

    @InjectMocks
    private CampaignService campaignService;

    private Pageable pageable = PageRequest.of(0, 10);

    @Test
    void createBasicCampaign_shouldSaveDraftCampaignWithWorkspaceId() {
        Long tenantId = 1L;
        Long workspaceId = 2L;

        when(campaignRepository.save(any(Campaign.class))).thenAnswer(i -> i.getArgument(0));

        CampaignDto dto = campaignService.createBasicCampaign(tenantId, workspaceId);

        assertNotNull(dto);
        assertEquals(workspaceId, dto.getWorkspaceId());
        assertEquals(tenantId, dto.getTenantId());
        assertEquals(CampaignStatus.DRAFT, dto.getStatus());
        verify(campaignRepository).save(any(Campaign.class));
    }

    @Test
    void getCampaignById_shouldReturnDto_whenFound() {
        Campaign campaign = Campaign.builder().id(1L).workspaceId(3L).status(CampaignStatus.DRAFT).build();
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        CampaignDto dto = campaignService.getCampaignById(1L);

        assertEquals(1L, dto.getId());
        assertEquals(CampaignStatus.DRAFT, dto.getStatus());
        verify(campaignRepository).findById(1L);
    }

    @Test
    void getCampaignById_shouldThrow_whenNotFound() {
        when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> campaignService.getCampaignById(999L));
        verify(campaignRepository).findById(999L);
    }

    @Test
    void launchCampaignById_shouldThrow_whenNoAvailableMailboxes() {
        Campaign campaign = Campaign.builder().id(2L).status(CampaignStatus.DRAFT).workspaceId(4L).build();
        when(campaignRepository.findById(2L)).thenReturn(Optional.of(campaign));

        assertThrows(BadRequestException.class, () -> campaignService.launchCampaignById(1L, 4L, 2L));
    }

    @Test
    void launchCampaignById_shouldLaunch_whenValid() {
        Campaign campaign = Campaign.builder()
                .id(3L)
                .sendingMailboxId(5L)
                .status(CampaignStatus.DRAFT)
                .workspaceId(4L)
                .build();

        MailboxDto mailboxDto = MailboxDto.builder()
                .id(1L)
                .type(MailBoxType.OUTLOOK)
                .build();

        when(campaignRepository.findById(3L)).thenReturn(Optional.of(campaign));
        when(mailboxService.getAccessibleMailboxById(5L, 4L, 1L)).thenReturn(mailboxDto);

        campaignService.launchCampaignById(1L, 4L, 3L);

        assertEquals(CampaignStatus.RUNNING, campaign.getStatus());
        verify(campaignRepository).save(campaign);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void pauseCampaignById_shouldPauseRunningCampaignAndPublishEvent() {
        Campaign campaign = Campaign.builder().id(11L).status(CampaignStatus.RUNNING).workspaceId(1L).build();
        when(campaignRepository.findById(11L)).thenReturn(Optional.of(campaign));

        campaignService.pauseCampaignById(11L);

        assertEquals(CampaignStatus.PAUSED, campaign.getStatus());
        verify(campaignRepository).save(campaign);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void pauseCampaignById_shouldThrow_whenNotRunning() {
        Campaign campaign = Campaign.builder().id(13L).status(CampaignStatus.DRAFT).workspaceId(1L).build();
        when(campaignRepository.findById(13L)).thenReturn(Optional.of(campaign));

        assertThrows(BadRequestException.class, () -> campaignService.pauseCampaignById(13L));
    }

    @Test
    void resumeCampaignById_shouldResumePausedCampaignAndPublishEvent() {
        Campaign campaign = Campaign.builder().id(21L).status(CampaignStatus.PAUSED).workspaceId(1L).build();
        when(campaignRepository.findById(21L)).thenReturn(Optional.of(campaign));

        campaignService.resumeCampaignById(21L);

        assertEquals(CampaignStatus.RUNNING, campaign.getStatus());
        verify(campaignRepository).save(campaign);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void resumeCampaignById_shouldThrow_whenNotPaused() {
        Campaign campaign = Campaign.builder().id(23L).status(CampaignStatus.DRAFT).workspaceId(1L).build();
        when(campaignRepository.findById(23L)).thenReturn(Optional.of(campaign));

        assertThrows(BadRequestException.class, () -> campaignService.resumeCampaignById(23L));
    }

    @Test
    void deleteCampaignById_shouldPublishEventAndDeleteCampaign() {
        Campaign campaign = Campaign.builder().id(31L).workspaceId(100L).build();
        when(campaignRepository.findById(31L)).thenReturn(Optional.of(campaign));

        campaignService.deleteCampaignById(31L);

        verify(eventPublisher).publishEvent(any());
        verify(campaignRepository).delete(campaign);
    }

    @Test
    void getAllCampaigns_shouldReturnPage_withProgress() {
        Long tenantId = 10L;
        Long workspaceId = 11L;

        CampaignListDto dto = new CampaignListDto();
        dto.setId(1L);
        dto.setName("Campaign 1");
        dto.setSentEmails(5L);
        dto.setEmailSteps(10);
        dto.setTotalContacts(2);
        dto.setProgress(25.0);

        when(campaignSearchService.searchCampaigns(eq(tenantId), eq(workspaceId), any(), any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(dto), pageable, 1));

        Page<CampaignListDto> result = campaignSearchService.searchCampaigns(
                tenantId,
                workspaceId,
                "",
                List.of(),
                List.of(),
                pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().getFirst().getId());
    }

    @Test
    void updateCampaignRecipients_shouldUpdateRecipientsAndReturnDto() {
        Campaign campaign = Campaign.builder().id(1L).workspaceId(3L).build();
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(i -> i.getArgument(0));

        CampaignDto campaignDto = mock(CampaignDto.class);
        when(campaignDto.getCcRecipients()).thenReturn(List.of());
        when(campaignDto.getBccRecipients()).thenReturn(List.of());

        CampaignDto result = campaignService.updateCampaignRecipients(1L, campaignDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(campaignRepository).save(any(Campaign.class));
    }

    @Test
    void updateCampaignRecipients_shouldThrow_whenCampaignNotFound() {
        when(campaignRepository.findById(999L)).thenReturn(Optional.empty());
        CampaignDto campaignDto = mock(CampaignDto.class);

        assertThrows(ResourceNotFoundException.class, () -> campaignService.updateCampaignRecipients(999L, campaignDto));
    }
}
