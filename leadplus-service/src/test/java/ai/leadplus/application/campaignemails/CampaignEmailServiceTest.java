package ai.leadplus.application.campaignemails;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.campaigns.CampaignDto;
import ai.leadplus.application.campaigncontacts.CampaignContactInfoDto;
import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.application.leadcontact.LeadContactService;
import ai.leadplus.application.campaigns.SendingWindowService;
import ai.leadplus.domain.campaignemails.CampaignEmail;
import ai.leadplus.domain.campaignemails.CampaignEmailRepository;
import ai.leadplus.domain.campaignemails.CampaignEmailStatus;
import ai.leadplus.domain.campaigncontacts.CampaignContact;
import ai.leadplus.domain.campaigncontacts.CampaignContactRepository;
import ai.leadplus.domain.campaigncontacts.CampaignContactStatus;
import ai.leadplus.domain.campaigns.Campaign;
import ai.leadplus.domain.campaigns.CampaignStatus;
import ai.leadplus.domain.campaigns.CampaignRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CampaignEmailServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private CampaignEmailRepository campaignEmailRepository;

    @Mock
    private CampaignContactRepository campaignContactRepository;

    @Mock
    private LeadContactService leadContactService;

    @Mock
    private SendingWindowService sendingWindowService;

    @InjectMocks
    private CampaignEmailService campaignEmailService;

    @Test
    void getEmailSequencesByCampaignId_shouldReturnEmailSequences_whenCampaignExists() {
        Long campaignId = 1L;
        List<CampaignEmail> campaignEmails = List.of(
                new CampaignEmail(1L, campaignId, 1, "Subject 1", "Body 1", 3, null, null, null, null),
                new CampaignEmail(2L, campaignId, 2, "Subject 2", "Body 2", 4, null, null, null, null)
        );
        when(campaignEmailRepository.findAllByCampaignId(campaignId)).thenReturn(campaignEmails);

        List<CampaignEmailDto> result = campaignEmailService.getEmailSequencesByCampaignId(campaignId);

        assertEquals(2, result.size());
        assertEquals("Subject 1", result.getFirst().getSubject());
        verify(campaignEmailRepository).findAllByCampaignId(campaignId);
    }

    @Test
    void configureEmailSequences_shouldSaveDefaultEmailSequences_whenNoExistingSequences() {
        Long campaignId = 1L;
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setStatus(CampaignStatus.DRAFT);
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(campaignEmailRepository.existsByCampaignId(campaignId)).thenReturn(false);

        campaignEmailService.configureEmailSequences(campaignId);

        verify(campaignEmailRepository, times(1)).saveAll(anyList());
    }

    @Test
    void configureEmailSequences_shouldNotSaveEmailSequences_whenSequencesAlreadyExist() {
        Long campaignId = 1L;
        when(campaignEmailRepository.existsByCampaignId(campaignId)).thenReturn(true);

        campaignEmailService.configureEmailSequences(campaignId);

        verify(campaignEmailRepository, never()).saveAll(anyList());
    }

    @Test
    void configureEmailSequences_shouldThrow_whenCampaignIsRunningEvenIfSequencesAlreadyExist() {
        Long campaignId = 1L;
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setStatus(CampaignStatus.RUNNING);
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));

        assertThrows(BadRequestException.class, () -> campaignEmailService.configureEmailSequences(campaignId));

        verify(campaignEmailRepository, never()).saveAll(anyList());
    }

    @Test
    void createBasicCampaignEmail_shouldCopyPreviousEmailContent_whenPreviousStepExists() {
        Long campaignId = 1L;
        CampaignEmail previousEmail = new CampaignEmail(
                2L,
                campaignId,
                1,
                "Prev Subject",
                "Prev Body",
                3,
                List.of("a1", "a2"),
                CampaignEmailStatus.PENDING,
                null,
                null
        );

        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(new ai.leadplus.domain.campaigns.Campaign()));
        when(campaignEmailRepository.findTopByCampaignIdOrderByStepNumberDesc(campaignId)).thenReturn(Optional.of(previousEmail));
        when(campaignEmailRepository.save(any(CampaignEmail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CampaignEmailDto result = campaignEmailService.createBasicCampaignEmail(campaignId);

        assertEquals(2, result.getStepNumber());
        assertEquals("Prev Subject", result.getSubject());
        assertEquals("Prev Body", result.getBodyTemplate());
        assertEquals(List.of("a1", "a2"), result.getAttachmentIds());
        assertEquals(4, result.getDelayDays());
        verify(campaignEmailRepository).save(any(CampaignEmail.class));
        verify(campaignEmailRepository, never()).findByCampaignIdAndStepNumber(anyLong(), anyInt());
    }

    @Test
    void createBasicCampaignEmail_shouldUseDefaults_whenNoPreviousEmailExists() {
        Long campaignId = 1L;

        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(new ai.leadplus.domain.campaigns.Campaign()));
        when(campaignEmailRepository.findTopByCampaignIdOrderByStepNumberDesc(campaignId)).thenReturn(Optional.empty());
        when(campaignEmailRepository.save(any(CampaignEmail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CampaignEmailDto result = campaignEmailService.createBasicCampaignEmail(campaignId);

        assertEquals(1, result.getStepNumber());
        assertEquals("Partnership Opportunity with LeadPlus", result.getSubject());
        assertEquals("""
                Hi {firstName},
                
                I noticed that {companyName} is leading the way in the {industry} sector, and I wanted to reach out...""", result.getBodyTemplate());
        assertNull(result.getAttachmentIds());
        verify(campaignEmailRepository).save(any(CampaignEmail.class));
        verify(campaignEmailRepository, never()).findByCampaignIdAndStepNumber(anyLong(), anyInt());
    }

    @Test
    void createBasicCampaignEmail_shouldCreate_whenCampaignIsRunning() {
        Long campaignId = 1L;
        CampaignEmail previousEmail = new CampaignEmail(
                2L,
                campaignId,
                1,
                "Prev Subject",
                "Prev Body",
                3,
                List.of("a1", "a2"),
                CampaignEmailStatus.PENDING,
                null,
                null
        );
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setStatus(CampaignStatus.RUNNING);
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(campaignEmailRepository.findTopByCampaignIdOrderByStepNumberDesc(campaignId)).thenReturn(Optional.of(previousEmail));
        when(campaignEmailRepository.save(any(CampaignEmail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CampaignEmailDto result = campaignEmailService.createBasicCampaignEmail(campaignId);

        assertEquals(2, result.getStepNumber());
        assertEquals("Prev Subject", result.getSubject());
        assertEquals("Prev Body", result.getBodyTemplate());
        assertEquals(List.of("a1", "a2"), result.getAttachmentIds());
        verify(campaignEmailRepository).save(any(CampaignEmail.class));
    }

    @Test
    void createBasicCampaignEmail_shouldCreate_whenCampaignIsPaused() {
        Long campaignId = 1L;
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setStatus(CampaignStatus.PAUSED);
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(campaignEmailRepository.findTopByCampaignIdOrderByStepNumberDesc(campaignId)).thenReturn(Optional.empty());
        when(campaignEmailRepository.save(any(CampaignEmail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CampaignEmailDto result = campaignEmailService.createBasicCampaignEmail(campaignId);

        assertEquals(1, result.getStepNumber());
        assertEquals("Partnership Opportunity with LeadPlus", result.getSubject());
        verify(campaignEmailRepository).save(any(CampaignEmail.class));
    }

    @Test
    void updateNoOfSequences_shouldThrow_whenCampaignIsRunning() {
        Long campaignId = 1L;
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setStatus(CampaignStatus.RUNNING);
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));

        assertThrows(BadRequestException.class, () -> campaignEmailService.updateNoOfSequences(campaignId, 2));

        verify(campaignEmailRepository, never()).saveAll(anyList());
        verify(campaignEmailRepository, never()).deleteAll(anyList());
    }

    @Test
    void updateNoOfSequences_shouldThrow_whenCampaignIsPaused() {
        Long campaignId = 1L;
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setStatus(CampaignStatus.PAUSED);
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));

        assertThrows(BadRequestException.class, () -> campaignEmailService.updateNoOfSequences(campaignId, 2));

        verify(campaignEmailRepository, never()).saveAll(anyList());
        verify(campaignEmailRepository, never()).deleteAll(anyList());
    }

    @Test
    void deleteCampaignEmail_shouldDeleteAndResyncContacts_whenRunningCampaignAndDeletingNextPendingStep() {
        Long campaignId = 1L;
        Long emailId = 2L;

        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setTenantId(10L);
        campaign.setStatus(CampaignStatus.RUNNING);

        CampaignEmail step1 = new CampaignEmail(1L, campaignId, 1, "Subject 1", "Body 1", 0, null, CampaignEmailStatus.COMPLETED, null, null);
        CampaignEmail step2 = new CampaignEmail(2L, campaignId, 2, "Subject 2", "Body 2", 3, null, CampaignEmailStatus.PENDING, null, null);
        CampaignEmail step3 = new CampaignEmail(3L, campaignId, 3, "Subject 3", "Body 3", 4, null, CampaignEmailStatus.PENDING, null, null);
        CampaignEmail shiftedStep3 = new CampaignEmail(3L, campaignId, 2, "Subject 3", "Body 3", 4, null, CampaignEmailStatus.PENDING, null, null);

        CampaignContact contactAtStep2 = CampaignContact.builder()
                .id(11L)
                .campaignId(campaignId)
                .contactId(101L)
                .currentStep(2)
                .nextSendAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .status(CampaignContactStatus.ACTIVE)
                .build();
        CampaignContact contactAtStep3 = CampaignContact.builder()
                .id(12L)
                .campaignId(campaignId)
                .contactId(102L)
                .currentStep(3)
                .nextSendAt(LocalDateTime.of(2099, 1, 2, 0, 0))
                .status(CampaignContactStatus.ACTIVE)
                .build();
        LeadContactDto leadContact2 = LeadContactDto.builder()
                .id(101L)
                .locationCountry("US")
                .locationState("CA")
                .build();
        LeadContactDto leadContact3 = LeadContactDto.builder()
                .id(102L)
                .locationCountry("US")
                .locationState("CA")
                .build();

        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(campaignEmailRepository.findByIdAndCampaignId(emailId, campaignId)).thenReturn(Optional.of(step2));
        when(campaignEmailRepository.countByCampaignId(campaignId)).thenReturn(3L);
        when(campaignEmailRepository.findAllByCampaignIdAndStepNumberGreaterThan(campaignId, 2)).thenReturn(List.of(step3));
        when(campaignEmailRepository.findAllByCampaignId(campaignId)).thenReturn(List.of(step1, shiftedStep3));
        when(campaignContactRepository.findAllByCampaignIdAndStatusAndParticipatingTrue(campaignId, CampaignContactStatus.ACTIVE))
                .thenReturn(List.of(contactAtStep2, contactAtStep3));
        when(leadContactService.getLeadContactsByIds(List.of(102L))).thenReturn(List.of(leadContact3));
        when(campaignContactRepository.saveAllAndFlush(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(sendingWindowService.nextValidSendTime(any(LocalDateTime.class), any(CampaignContactInfoDto.class), any(CampaignDto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        campaignEmailService.deleteCampaignEmail(campaignId, emailId);

        ArgumentCaptor<List<CampaignContact>> contactCaptor = ArgumentCaptor.forClass(List.class);
        verify(campaignContactRepository).saveAllAndFlush(contactCaptor.capture());
        List<CampaignContact> savedContacts = contactCaptor.getValue();
        assertEquals(2, savedContacts.size());
        CampaignContact savedContactAtStep2 = savedContacts.stream().filter(contact -> contact.getContactId().equals(101L)).findFirst().orElseThrow();
        CampaignContact savedContactAtStep3 = savedContacts.stream().filter(contact -> contact.getContactId().equals(102L)).findFirst().orElseThrow();
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), savedContactAtStep2.getNextSendAt());
        assertEquals(CampaignContactStatus.ACTIVE, savedContactAtStep2.getStatus());
        assertEquals(2, savedContactAtStep3.getCurrentStep());
        assertEquals(CampaignContactStatus.ACTIVE, savedContactAtStep3.getStatus());
        assertEquals(LocalDateTime.of(2099, 1, 2, 0, 0), savedContactAtStep3.getNextSendAt());

        verify(campaignEmailRepository).delete(step2);
        verify(campaignEmailRepository).saveAllAndFlush(anyList());
        verify(leadContactService).getLeadContactsByIds(List.of(102L));
        verify(sendingWindowService, times(1)).nextValidSendTime(any(LocalDateTime.class), any(CampaignContactInfoDto.class), any(CampaignDto.class));
    }

    @Test
    void deleteCampaignEmail_shouldDeleteAndResyncContacts_whenPausedCampaignAndDeletingNextPendingStep() {
        Long campaignId = 1L;
        Long emailId = 2L;

        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setTenantId(10L);
        campaign.setStatus(CampaignStatus.PAUSED);

        CampaignEmail step1 = new CampaignEmail(1L, campaignId, 1, "Subject 1", "Body 1", 0, null, CampaignEmailStatus.COMPLETED, null, null);
        CampaignEmail step2 = new CampaignEmail(2L, campaignId, 2, "Subject 2", "Body 2", 3, null, CampaignEmailStatus.PENDING, null, null);
        CampaignEmail step3 = new CampaignEmail(3L, campaignId, 3, "Subject 3", "Body 3", 4, null, CampaignEmailStatus.PENDING, null, null);
        CampaignEmail shiftedStep3 = new CampaignEmail(3L, campaignId, 2, "Subject 3", "Body 3", 4, null, CampaignEmailStatus.PENDING, null, null);

        CampaignContact contactAtStep2 = CampaignContact.builder()
                .id(11L)
                .campaignId(campaignId)
                .contactId(101L)
                .currentStep(2)
                .nextSendAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .status(CampaignContactStatus.ACTIVE)
                .build();
        CampaignContact contactAtStep3 = CampaignContact.builder()
                .id(12L)
                .campaignId(campaignId)
                .contactId(102L)
                .currentStep(3)
                .nextSendAt(LocalDateTime.of(2026, 1, 2, 0, 0))
                .status(CampaignContactStatus.ACTIVE)
                .build();
        LeadContactDto leadContact2 = LeadContactDto.builder()
                .id(101L)
                .locationCountry("US")
                .locationState("CA")
                .build();
        LeadContactDto leadContact3 = LeadContactDto.builder()
                .id(102L)
                .locationCountry("US")
                .locationState("CA")
                .build();

        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(campaignEmailRepository.findByIdAndCampaignId(emailId, campaignId)).thenReturn(Optional.of(step2));
        when(campaignEmailRepository.countByCampaignId(campaignId)).thenReturn(3L);
        when(campaignEmailRepository.findAllByCampaignIdAndStepNumberGreaterThan(campaignId, 2)).thenReturn(List.of(step3));
        when(campaignEmailRepository.findAllByCampaignId(campaignId)).thenReturn(List.of(step1, shiftedStep3));
        when(campaignContactRepository.findAllByCampaignIdAndStatusAndParticipatingTrue(campaignId, CampaignContactStatus.ACTIVE))
                .thenReturn(List.of(contactAtStep2, contactAtStep3));
        when(leadContactService.getLeadContactsByIds(List.of(102L))).thenReturn(List.of(leadContact3));
        when(campaignContactRepository.saveAllAndFlush(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(sendingWindowService.nextValidSendTime(any(LocalDateTime.class), any(CampaignContactInfoDto.class), any(CampaignDto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        campaignEmailService.deleteCampaignEmail(campaignId, emailId);

        ArgumentCaptor<List<CampaignContact>> contactCaptor = ArgumentCaptor.forClass(List.class);
        verify(campaignContactRepository).saveAllAndFlush(contactCaptor.capture());
        List<CampaignContact> savedContacts = contactCaptor.getValue();
        assertEquals(2, savedContacts.size());
        CampaignContact savedContactAtStep2 = savedContacts.stream().filter(contact -> contact.getContactId().equals(101L)).findFirst().orElseThrow();
        CampaignContact savedContactAtStep3 = savedContacts.stream().filter(contact -> contact.getContactId().equals(102L)).findFirst().orElseThrow();
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), savedContactAtStep2.getNextSendAt());
        assertEquals(CampaignContactStatus.ACTIVE, savedContactAtStep2.getStatus());
        assertEquals(2, savedContactAtStep3.getCurrentStep());
        assertEquals(CampaignContactStatus.ACTIVE, savedContactAtStep3.getStatus());
        assertTrue(savedContactAtStep3.getNextSendAt().isAfter(LocalDateTime.of(2026, 1, 2, 0, 0)));

        verify(campaignEmailRepository).delete(step2);
        verify(campaignEmailRepository).saveAllAndFlush(anyList());
        verify(leadContactService).getLeadContactsByIds(List.of(102L));
        verify(sendingWindowService, times(1)).nextValidSendTime(any(LocalDateTime.class), any(CampaignContactInfoDto.class), any(CampaignDto.class));
    }

    @Test
    void deleteCampaignEmail_shouldDelete_whenRunningCampaignAndTargetIsAnyPendingStep() {
        Long campaignId = 1L;
        Long tenantId = 10L;
        Long emailId = 3L;

        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setTenantId(tenantId);
        campaign.setStatus(CampaignStatus.RUNNING);

        CampaignEmail step1 = new CampaignEmail(1L, campaignId, 1, "Subject 1", "Body 1", 0, null, CampaignEmailStatus.COMPLETED, null, null);
        CampaignEmail step2 = new CampaignEmail(2L, campaignId, 2, "Subject 2", "Body 2", 3, null, CampaignEmailStatus.PENDING, null, null);
        CampaignEmail step3 = new CampaignEmail(3L, campaignId, 3, "Subject 3", "Body 3", 4, null, CampaignEmailStatus.PENDING, null, null);

        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(campaignEmailRepository.findByIdAndCampaignId(emailId, campaignId)).thenReturn(Optional.of(step3));
        when(campaignEmailRepository.countByCampaignId(campaignId)).thenReturn(3L);
        when(campaignEmailRepository.findAllByCampaignIdAndStepNumberGreaterThan(campaignId, 3)).thenReturn(List.of());
        when(campaignContactRepository.findAllByCampaignIdAndStatusAndParticipatingTrue(campaignId, CampaignContactStatus.ACTIVE))
                .thenReturn(List.of());

        campaignEmailService.deleteCampaignEmail(campaignId, emailId);

        verify(campaignEmailRepository).delete(step3);
        verify(campaignEmailRepository).saveAllAndFlush(anyList());
        verify(campaignContactRepository, never()).saveAllAndFlush(anyList());
        verifyNoInteractions(leadContactService, sendingWindowService);
    }

    @Test
    void deleteCampaignEmail_shouldCompleteContacts_whenDeletingLastPendingStep() {
        Long campaignId = 1L;
        Long emailId = 2L;

        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setTenantId(10L);
        campaign.setStatus(CampaignStatus.RUNNING);

        CampaignEmail step1 = new CampaignEmail(1L, campaignId, 1, "Subject 1", "Body 1", 0, null, CampaignEmailStatus.COMPLETED, null, null);
        CampaignEmail step2 = new CampaignEmail(2L, campaignId, 2, "Subject 2", "Body 2", 3, null, CampaignEmailStatus.PENDING, null, null);

        CampaignContact contactAtStep2 = CampaignContact.builder()
                .id(11L)
                .campaignId(campaignId)
                .contactId(101L)
                .currentStep(2)
                .nextSendAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .status(CampaignContactStatus.ACTIVE)
                .build();

        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(campaignEmailRepository.findByIdAndCampaignId(emailId, campaignId)).thenReturn(Optional.of(step2));
        when(campaignEmailRepository.countByCampaignId(campaignId)).thenReturn(2L);
        when(campaignEmailRepository.findAllByCampaignIdAndStepNumberGreaterThan(campaignId, 2)).thenReturn(List.of());
        when(campaignEmailRepository.findAllByCampaignId(campaignId)).thenReturn(List.of(step1));
        when(campaignContactRepository.findAllByCampaignIdAndStatusAndParticipatingTrue(campaignId, CampaignContactStatus.ACTIVE))
                .thenReturn(List.of(contactAtStep2));
        when(campaignContactRepository.saveAllAndFlush(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        campaignEmailService.deleteCampaignEmail(campaignId, emailId);

        ArgumentCaptor<List<CampaignContact>> contactCaptor = ArgumentCaptor.forClass(List.class);
        verify(campaignContactRepository).saveAllAndFlush(contactCaptor.capture());
        List<CampaignContact> savedContacts = contactCaptor.getValue();
        assertEquals(1, savedContacts.size());
        assertEquals(CampaignContactStatus.COMPLETED, savedContacts.getFirst().getStatus());
        assertNull(savedContacts.getFirst().getNextSendAt());
        verifyNoInteractions(leadContactService, sendingWindowService);
    }

    @Test
    void deleteCampaignEmail_shouldThrow_whenRunningCampaignHasOnlyOneEmail() {
        Long campaignId = 1L;
        Long emailId = 2L;

        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setTenantId(10L);
        campaign.setStatus(CampaignStatus.RUNNING);

        CampaignEmail step1 = new CampaignEmail(2L, campaignId, 1, "Subject 1", "Body 1", 0, null, CampaignEmailStatus.PENDING, null, null);

        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(campaignEmailRepository.findByIdAndCampaignId(emailId, campaignId)).thenReturn(Optional.of(step1));
        when(campaignEmailRepository.countByCampaignId(campaignId)).thenReturn(1L);

        assertThrows(BadRequestException.class, () -> campaignEmailService.deleteCampaignEmail(campaignId, emailId));

        verify(campaignEmailRepository, never()).delete(any());
        verify(campaignEmailRepository, never()).saveAllAndFlush(anyList());
        verifyNoInteractions(campaignContactRepository, leadContactService, sendingWindowService);
    }

    @Test
    void updateCampaignEmail_shouldUpdateAndReturnUpdatedEmail_whenEmailExists() {
        Long emailId = 1L;
        CampaignEmail existingEmail = new CampaignEmail(1L, 2L, 1, "Old Subject", "Old Body", 3, null, CampaignEmailStatus.PENDING, null, null);
        CampaignEmailDto updateDto = CampaignEmailDto.builder()
                .id(1L)
                .subject("New Subject")
                .bodyTemplate("New Body")
                .delayDays(5)
                .build();
        when(campaignEmailRepository.findById(1L)).thenReturn(Optional.of(existingEmail));

        CampaignEmailDto result = campaignEmailService.updateCampaignEmail(emailId, updateDto);

        assertEquals("New Subject", result.getSubject());
        assertEquals(5, result.getDelayDays());
        verify(campaignEmailRepository).save(existingEmail);
    }

    @Test
    void updateCampaignEmail_shouldThrowResourceNotFoundException_whenEmailDoesNotExist() {
        Long emailId = 1L;
        CampaignEmailDto updateDto = CampaignEmailDto.builder()
                .id(1L)
                .subject("New Subject")
                .bodyTemplate("New Body")
                .delayDays(5)
                .build();
        when(campaignEmailRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> campaignEmailService.updateCampaignEmail(emailId, updateDto));
        verify(campaignEmailRepository).findById(1L);
    }
}
