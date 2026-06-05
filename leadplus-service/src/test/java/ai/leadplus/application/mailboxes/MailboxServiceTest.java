package ai.leadplus.application.mailboxes;

import ai.leadplus.application.campaignorchestrator.CampaignEmailSentEvent;
import ai.leadplus.application.campaigns.CampaignAssignmentService;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.exception.UnauthorizedException;
import ai.leadplus.domain.mailboxes.*;
import ai.leadplus.application.workspaceuser.WorkspaceUserDto;
import ai.leadplus.application.workspaceuser.WorkspaceUserService;
import ai.leadplus.domain.workspaceusers.WorkspaceUserRole;
import ai.leadplus.infrastructure.aws.ses.AwsSESAuthClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailboxServiceTest {

    @Mock
    private MailboxRepository mailboxRepository;

    @Mock
    private AwsSESAuthClient awsSESAuthClient;

    @Mock
    private CampaignAssignmentService campaignAssignmentService;

    @Mock
    private WorkspaceUserService workspaceUserService;

    @InjectMocks
    private MailboxService mailboxService;

    @BeforeEach
    void setUp() {
        lenient().when(workspaceUserService.findByWorkspaceIdAndUserId(anyLong(), anyLong()))
                .thenReturn(WorkspaceUserDto.builder().role(WorkspaceUserRole.OWNER).build());
    }

    @Test
    void updateAzureDetails_shouldUpdateExistingMailbox() {
        Long userId = 1L;
        Long workspaceId = 1L;
        String azureId = "azure-1";
        String email = "user@example.com";
        String refreshToken = "ref-token";

        Mailbox existing = Mailbox.builder()
                .id(1L)
                .userId(userId)
                .workspaceId(workspaceId)
                .type(MailBoxType.OUTLOOK)
                .active(true)
                .metaData(MailboxProviderConfig.builder().build())
                .build();

        when(mailboxRepository.findByUserIdAndWorkspaceIdAndTypeAndActiveTrue(userId, workspaceId, MailBoxType.OUTLOOK))
                .thenReturn(Optional.of(existing));

        mailboxService.updateAzureDetails(userId, workspaceId, azureId, email, refreshToken);

        ArgumentCaptor<Mailbox> captor = ArgumentCaptor.forClass(Mailbox.class);
        verify(mailboxRepository).save(captor.capture());
        Mailbox saved = captor.getValue();

        assertEquals(email, saved.getEmailAddress());
        assertNotNull(saved.getMetaData());
        assertEquals(azureId, saved.getMetaData().getAzureId());
        assertEquals(refreshToken, saved.getMetaData().getRefreshToken());
        assertTrue(saved.isActive());
    }

    @Test
    void updateAzureDetails_shouldCreateMailboxWhenNoneExists() {
        Long userId = 1L;
        Long workspaceId = 1L;
        String azureId = "azure-2";
        String email = "new@example.com";
        String refreshToken = "rt-2";

        when(mailboxRepository.findByUserIdAndWorkspaceIdAndTypeAndActiveTrue(userId, workspaceId, MailBoxType.OUTLOOK))
                .thenReturn(Optional.empty());

        when(mailboxRepository.save(any(Mailbox.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mailboxService.updateAzureDetails(userId, workspaceId, azureId, email, refreshToken);

        ArgumentCaptor<Mailbox> captor = ArgumentCaptor.forClass(Mailbox.class);
        verify(mailboxRepository).save(captor.capture());
        Mailbox saved = captor.getValue();

        assertEquals(userId, saved.getUserId());
        assertEquals(workspaceId, saved.getWorkspaceId());
        assertEquals(email, saved.getEmailAddress());
        assertEquals(MailBoxType.OUTLOOK, saved.getType());
        assertNotNull(saved.getMetaData());
        assertEquals(azureId, saved.getMetaData().getAzureId());
    }

    @Test
    void updateGoogleDetails_shouldUpdateExistingMailbox() {
        Long userId = 1L;
        Long workspaceId = 1L;
        String googleId = "g-id-1";
        String email = "guser@example.com";
        String refreshToken = "g-ref-token";
        String labelId = "label-1";

        Mailbox existing = Mailbox.builder()
                .id(2L)
                .userId(userId)
                .workspaceId(workspaceId)
                .type(MailBoxType.GMAIL)
                .active(true)
                .metaData(MailboxProviderConfig.builder().build())
                .build();

        when(mailboxRepository.findByUserIdAndWorkspaceIdAndTypeAndActiveTrue(userId, workspaceId, MailBoxType.GMAIL))
                .thenReturn(Optional.of(existing));

        mailboxService.updateGoogleDetails(userId, workspaceId, googleId, email, refreshToken, labelId);

        ArgumentCaptor<Mailbox> captor = ArgumentCaptor.forClass(Mailbox.class);
        verify(mailboxRepository).save(captor.capture());
        Mailbox saved = captor.getValue();

        assertEquals(email, saved.getEmailAddress());
        assertNotNull(saved.getMetaData());
        assertEquals(googleId, saved.getMetaData().getGoogleId());
        assertEquals(refreshToken, saved.getMetaData().getRefreshToken());
        assertTrue(saved.isActive());
    }

    @Test
    void updateGoogleDetails_shouldCreateMailboxWhenNoneExists() {
        Long userId = 1L;
        Long workspaceId = 1L;
        String googleId = "g-id-2";
        String email = "newg@example.com";
        String refreshToken = "g-rt-2";
        String labelId = "label-1";

        when(mailboxRepository.findByUserIdAndWorkspaceIdAndTypeAndActiveTrue(userId, workspaceId, MailBoxType.GMAIL))
                .thenReturn(Optional.empty());

        when(mailboxRepository.save(any(Mailbox.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mailboxService.updateGoogleDetails(userId, workspaceId, googleId, email, refreshToken, labelId);

        ArgumentCaptor<Mailbox> captor = ArgumentCaptor.forClass(Mailbox.class);
        verify(mailboxRepository).save(captor.capture());
        Mailbox saved = captor.getValue();

        assertEquals(userId, saved.getUserId());
        assertEquals(workspaceId, saved.getWorkspaceId());
        assertEquals(email, saved.getEmailAddress());
        assertEquals(MailBoxType.GMAIL, saved.getType());
        assertNotNull(saved.getMetaData());
        assertEquals(googleId, saved.getMetaData().getGoogleId());
        assertEquals(refreshToken, saved.getMetaData().getRefreshToken());
    }

    @Test
    void getOrCreateSesDetails_shouldReturnExistingMailboxDto_whenFound() {
        Long userId = 1L;
        Long workspaceId = 1L;
        String email = "ses@example.com";

        Mailbox existing = Mailbox.builder()
                .id(3L)
                .userId(userId)
                .workspaceId(workspaceId)
                .emailAddress(email)
                .type(MailBoxType.SES)
                .active(true)
                .metaData(MailboxProviderConfig.builder()
                        .awsSESVerificationState(AwsSESVerificationState.VERIFIED)
                        .connectedAt(LocalDateTime.now())
                        .build())
                .build();

        when(mailboxRepository.findByUserIdAndWorkspaceIdAndTypeAndActiveTrue(userId, workspaceId, MailBoxType.SES))
                .thenReturn(Optional.of(existing));

        MailboxDto dto = mailboxService.getOrCreateSesDetails(userId, email, workspaceId);

        assertEquals(existing.getId(), dto.getId());
        assertEquals(email, dto.getEmailAddress());
    }

    @Test
    void getOrCreateSesDetails_shouldCreateAndReturnNewMailboxDto_whenNotFound() {
        Long userId = 1L;
        Long workspaceId = 1L;
        String email = "create-ses@example.com";

        when(mailboxRepository.findByUserIdAndWorkspaceIdAndTypeAndActiveTrue(userId, workspaceId, MailBoxType.SES))
                .thenReturn(Optional.empty());

        when(mailboxRepository.save(any(Mailbox.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MailboxDto dto = mailboxService.getOrCreateSesDetails(userId, email, workspaceId);

        assertEquals(email, dto.getEmailAddress());
        assertEquals(MailBoxType.SES, dto.getType());
        assertEquals(AwsSESVerificationState.UNVERIFIED, dto.getMetaData().getAwsSESVerificationState());
    }

    @Test
    void getConnectedMailboxes_shouldReturnOnlyOwnedMailboxesAsDtoList() {
        Long userId = 1L;
        Long workspaceId = 1L;

        Mailbox m1 = Mailbox.builder().id(4L).userId(userId).workspaceId(workspaceId).emailAddress("a@x.com").active(true).type(MailBoxType.SES).build();
        Mailbox m2 = Mailbox.builder().id(5L).userId(2L).workspaceId(workspaceId).emailAddress("b@x.com").active(true).type(MailBoxType.OUTLOOK).build();

        when(mailboxRepository.findAllByUserIdAndWorkspaceIdAndActiveTrue(userId, workspaceId)).thenReturn(List.of(m1));

        List<MailboxDto> result = mailboxService.getConnectedMailboxes(workspaceId, userId);

        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(d -> d.getId().equals(4L)));
        assertFalse(result.stream().anyMatch(d -> d.getId().equals(5L)));
        verify(mailboxRepository, never()).findAllByWorkspaceIdAndActiveTrue(workspaceId);
    }

    @Test
    void getAuthorizedMailboxes_shouldReturnWorkspaceMailboxes_whenUserIsWorkspaceAdmin() {
        Long userId = 1L;
        Long workspaceId = 1L;
        Mailbox owned = Mailbox.builder().id(4L).userId(userId).workspaceId(workspaceId).emailAddress("a@x.com").active(true).type(MailBoxType.SES).build();
        Mailbox shared = Mailbox.builder().id(5L).userId(2L).workspaceId(workspaceId).emailAddress("b@x.com").active(true).type(MailBoxType.OUTLOOK).build();

        when(workspaceUserService.findByWorkspaceIdAndUserId(workspaceId, userId))
                .thenReturn(WorkspaceUserDto.builder().role(WorkspaceUserRole.WORKSPACE_ADMIN).build());
        when(mailboxRepository.findAllByWorkspaceIdAndActiveTrue(workspaceId)).thenReturn(List.of(owned, shared));

        List<MailboxDto> result = mailboxService.getAuthorizedMailboxes(workspaceId, userId);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(d -> d.getId().equals(4L)));
        assertTrue(result.stream().anyMatch(d -> d.getId().equals(5L)));
    }

    @Test
    void deactivateMailbox_shouldSetInactiveAndUnsetSesVerification_andRemoveIdentityIfVerified() {
        Mailbox mailbox = Mailbox.builder()
                .id(6L)
                .userId(1L)
                .workspaceId(1L)
                .emailAddress("remove@ses.com")
                .type(MailBoxType.SES)
                .active(true)
                .metaData(MailboxProviderConfig.builder()
                        .awsSESVerificationState(AwsSESVerificationState.VERIFIED)
                        .build())
                .build();

        when(mailboxRepository.findByIdAndActiveTrue(6L)).thenReturn(Optional.of(mailbox));
        when(campaignAssignmentService.isSendingMailboxAssigned(6L)).thenReturn(false);

        mailboxService.deactivateMailbox(6L, 1L, 1L);

        assertFalse(mailbox.isActive());
        assertEquals(AwsSESVerificationState.UNVERIFIED, mailbox.getMetaData().getAwsSESVerificationState());
        verify(awsSESAuthClient).removeEmailIdentity("remove@ses.com");
        verify(mailboxRepository).save(mailbox);
    }

    @Test
    void deactivateMailbox_shouldSetInactiveAndNotCallRemoveIdentity_whenSesAlreadyUnverified() {
        Mailbox mailbox = Mailbox.builder()
                .id(7L)
                .userId(1L)
                .workspaceId(1L)
                .emailAddress("noaction@ses.com")
                .type(MailBoxType.SES)
                .active(true)
                .metaData(MailboxProviderConfig.builder()
                        .awsSESVerificationState(AwsSESVerificationState.UNVERIFIED)
                        .build())
                .build();

        when(mailboxRepository.findByIdAndActiveTrue(7L)).thenReturn(Optional.of(mailbox));
        when(campaignAssignmentService.isSendingMailboxAssigned(7L)).thenReturn(false);

        mailboxService.deactivateMailbox(7L, 1L, 1L);

        assertFalse(mailbox.isActive());
        assertEquals(AwsSESVerificationState.UNVERIFIED, mailbox.getMetaData().getAwsSESVerificationState());
        verify(awsSESAuthClient, never()).removeEmailIdentity(any());
        verify(mailboxRepository).save(mailbox);
    }

    @Test
    void deactivateMailbox_shouldThrowWhenMailboxNotFound() {
        when(mailboxRepository.findByIdAndActiveTrue(0L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> mailboxService.deactivateMailbox(0L, 1L, 1L));
    }

    @Test
    void deactivateMailbox_shouldThrowWhenWorkspaceDoesNotMatch() {
        Mailbox mailbox = Mailbox.builder()
                .id(9L)
                .workspaceId(2L)
                .emailAddress("mismatch@ses.com")
                .type(MailBoxType.SES)
                .active(true)
                .metaData(MailboxProviderConfig.builder()
                        .awsSESVerificationState(AwsSESVerificationState.VERIFIED)
                        .build())
                .build();

        when(mailboxRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.of(mailbox));

        assertThrows(UnauthorizedException.class, () -> mailboxService.deactivateMailbox(9L, 1L, 1L));
        verify(campaignAssignmentService, never()).isSendingMailboxAssigned(anyLong());
        verify(mailboxRepository, never()).save(any());
    }

    @Test
    void deactivateMailbox_shouldThrowWhenMailboxBelongsToAnotherUserEvenInSameWorkspace() {
        Mailbox mailbox = Mailbox.builder()
                .id(13L)
                .userId(2L)
                .workspaceId(1L)
                .emailAddress("other-user@ses.com")
                .type(MailBoxType.SES)
                .active(true)
                .metaData(MailboxProviderConfig.builder()
                        .awsSESVerificationState(AwsSESVerificationState.VERIFIED)
                        .build())
                .build();

        when(mailboxRepository.findByIdAndActiveTrue(13L)).thenReturn(Optional.of(mailbox));

        assertThrows(UnauthorizedException.class, () -> mailboxService.deactivateMailbox(13L, 1L, 1L));
        verify(campaignAssignmentService, never()).isSendingMailboxAssigned(anyLong());
        verify(awsSESAuthClient, never()).removeEmailIdentity(any());
        verify(mailboxRepository, never()).save(any());
    }

    @Test
    void getMailboxById_shouldReturnDto_whenFound() {
        Mailbox mailbox = Mailbox.builder().id(8L).emailAddress("g@x.com").active(true).build();
        when(mailboxRepository.findByIdAndActiveTrue(8L)).thenReturn(Optional.of(mailbox));

        MailboxDto dto = mailboxService.getMailboxById(8L);

        assertEquals(8L, dto.getId());
        assertEquals("g@x.com", dto.getEmailAddress());
    }

    @Test
    void getConnectedMailboxes_shouldReturnOnlyOwnedMailboxes_whenUserIsNotWorkspaceAdmin() {
        Long userId = 2L;
        Long workspaceId = 1L;

        Mailbox owned = Mailbox.builder().id(21L).userId(userId).workspaceId(workspaceId).emailAddress("owned@x.com").active(true).type(MailBoxType.GMAIL).build();

        when(mailboxRepository.findAllByUserIdAndWorkspaceIdAndActiveTrue(userId, workspaceId)).thenReturn(List.of(owned));

        List<MailboxDto> result = mailboxService.getConnectedMailboxes(workspaceId, userId);

        assertEquals(1, result.size());
        assertEquals(21L, result.get(0).getId());
        verify(mailboxRepository, never()).findAllByWorkspaceIdAndActiveTrue(workspaceId);
    }

    @Test
    void getMailboxById_shouldThrow_whenNotFound() {
        when(mailboxRepository.findByIdAndActiveTrue(eq(9L))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> mailboxService.getMailboxById(9L));
    }

    @Test
    void saveMailbox_shouldPersistConvertedEntity() {
        MailboxProviderConfigDto cfgDto = MailboxProviderConfigDto.builder().build();
        MailboxDto dto = MailboxDto.builder().id(10L).userId(1L).workspaceId(2L).emailAddress("e@x").type(MailBoxType.SES).metaData(cfgDto).active(true).build();

        mailboxService.saveMailbox(dto);

        verify(mailboxRepository).save(any(Mailbox.class));
    }

    @Test
    void resetMailboxEmailCountDaily_shouldReturnCountFromRepository() {
        when(mailboxRepository.resetEmailsSentTodayForActiveMailboxes(0)).thenReturn(5L);

        long count = mailboxService.resetMailboxEmailCountDaily();

        assertEquals(5L, count);
    }

    @Test
    void handleCampaignEmailSendEvent_shouldIncrementEmailsSentToday() {
        Mailbox mailbox = Mailbox.builder().id(11L).emailsSentToday(2).active(true).build();
        MailboxDto dto = MailboxDto.builder().id(11L).build();

        when(mailboxRepository.findByIdAndActiveTrue(11L)).thenReturn(Optional.of(mailbox));

        CampaignEmailSentEvent event = new CampaignEmailSentEvent(this, 1L, 1L, dto, null, null, null);

        mailboxService.handleCampaignEmailSendEvent(event);

        assertEquals(3, mailbox.getEmailsSentToday());
        verify(mailboxRepository).save(mailbox);
    }

    @Test
    void updateRefreshToken_shouldUpdate_metaDataRefreshToken() {
        Mailbox mailbox = Mailbox.builder().id(12L).metaData(MailboxProviderConfig.builder().refreshToken("old").build()).active(true).build();
        when(mailboxRepository.findByIdAndActiveTrue(12L)).thenReturn(Optional.of(mailbox));

        AzureRefreshTokenUpdatedEvent event = new AzureRefreshTokenUpdatedEvent(this, "12", "new-token");

        mailboxService.updateRefreshToken(event);

        assertEquals("new-token", mailbox.getMetaData().getRefreshToken());
        verify(mailboxRepository).save(mailbox);
    }
}
