package ai.leadplus.application.campaigncontacts;

import ai.leadplus.application.campaigns.CampaignDto;
import ai.leadplus.application.campaigns.CampaignService;
import ai.leadplus.application.mailboxes.MailboxDto;
import ai.leadplus.application.mailboxes.MailboxProviderConfigDto;
import ai.leadplus.application.mailboxes.MailboxService;
import ai.leadplus.domain.mailboxes.MailBoxType;
import ai.leadplus.infrastructure.azure.auth.AzureAuthClient;
import ai.leadplus.infrastructure.azure.auth.AzureRefreshTokenResponse;
import ai.leadplus.infrastructure.azure.graph.emails.AzureConversationResponse;
import ai.leadplus.infrastructure.azure.graph.emails.AzureEmailClient;
import ai.leadplus.infrastructure.azure.graph.emails.AzureGraphMessageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureReplySyncServiceTest {

    @Mock private CampaignContactService campaignContactService;
    @Mock private CampaignService campaignService;
    @Mock private MailboxService mailboxService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private AzureEmailClient azureEmailClient;
    @Mock private AzureAuthClient azureAuthClient;

    private AzureReplySyncService service;

    private static final long CAMPAIGN_ID = 10L;
    private static final long MAILBOX_ID = 20L;
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String AZURE_ID = "azure-user-id";
    private static final String CONVERSATION_ID = "conv-abc";
    private static final String SENDER = "sender@example.com";
    private static final String RECIPIENT = "recipient@example.com";

    @BeforeEach
    void setUp() {
        service = new AzureReplySyncService(
                campaignContactService, campaignService, mailboxService,
                eventPublisher, azureEmailClient, azureAuthClient
        );
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Page<CampaignContactDto> singlePage(CampaignContactDto contact) {
        return new PageImpl<>(List.of(contact));
    }

    private CampaignContactDto contactWith(EmailDataDto... emailData) {
        CampaignContactDto contact = new CampaignContactDto();
        contact.setId(1L);
        contact.setCampaignId(CAMPAIGN_ID);
        contact.setReplyReceived(false);
        contact.setEmailData(List.of(emailData));
        return contact;
    }

    private EmailDataDto sentOutlookEmailData() {
        EmailDataDto ed = new EmailDataDto();
        ed.setEmailPlatform(MailBoxType.OUTLOOK);
        ed.setEmailDeliveryStatus(EmailDeliveryStatus.SENT);
        ed.setConversationId(CONVERSATION_ID);
        ed.setStepNumber(1);
        return ed;
    }

    private CampaignDto campaign() {
        CampaignDto c = new CampaignDto();
        c.setId(CAMPAIGN_ID);
        c.setSendingMailboxId(MAILBOX_ID);
        return c;
    }

    private MailboxDto mailbox() {
        return MailboxDto.builder()
                .id(MAILBOX_ID)
                .metaData(MailboxProviderConfigDto.builder()
                        .refreshToken(REFRESH_TOKEN)
                        .azureId(AZURE_ID)
                        .build())
                .build();
    }

    private AzureRefreshTokenResponse token() {
        AzureRefreshTokenResponse t = new AzureRefreshTokenResponse();
        t.setAccess_token(ACCESS_TOKEN);
        return t;
    }

    private AzureGraphMessageResponse azureMessage(String fromEmail, String toEmail) {
        AzureGraphMessageResponse.Sender.EmailAddress senderAddr =
                new AzureGraphMessageResponse.Sender.EmailAddress();
        senderAddr.setAddress(fromEmail);
        AzureGraphMessageResponse.Sender sender = new AzureGraphMessageResponse.Sender();
        sender.setEmailAddress(senderAddr);

        AzureGraphMessageResponse.Recipient.EmailAddress recipientAddr =
                new AzureGraphMessageResponse.Recipient.EmailAddress();
        recipientAddr.setAddress(toEmail);
        AzureGraphMessageResponse.Recipient recipient = new AzureGraphMessageResponse.Recipient();
        recipient.setEmailAddress(recipientAddr);

        AzureGraphMessageResponse.Body body = new AzureGraphMessageResponse.Body();
        body.setContent("Hello");

        AzureGraphMessageResponse msg = new AzureGraphMessageResponse();
        msg.setSender(sender);
        msg.setToRecipients(List.of(recipient));
        msg.setCcRecipients(List.of());
        msg.setBccRecipients(List.of());
        msg.setSubject("Test Subject");
        msg.setBody(body);
        return msg;
    }

    private AzureConversationResponse conversation(AzureGraphMessageResponse... messages) {
        AzureConversationResponse response = new AzureConversationResponse();
        response.setValue(List.of(messages));
        return response;
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    void syncReplies_replyDetected_setsStatusAndReplyReceived() {
        when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                eq(MailBoxType.OUTLOOK), eq(EmailDeliveryStatus.SENT), any(), any()))
                .thenReturn(singlePage(contactWith(sentOutlookEmailData())));
        when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
        when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
        when(azureAuthClient.fetchAccessTokenAndRefreshTokenFromRefreshToken(REFRESH_TOKEN))
                .thenReturn(token());
        when(azureEmailClient.getConversationById(AZURE_ID, CONVERSATION_ID, ACCESS_TOKEN))
                .thenReturn(conversation(
                        azureMessage(SENDER, RECIPIENT),
                        azureMessage(RECIPIENT, SENDER)
                ));
        when(campaignContactService.saveCampaignContact(any())).thenAnswer(i -> i.getArgument(0));

        service.syncReplies();

        ArgumentCaptor<CampaignContactDto> captor = ArgumentCaptor.forClass(CampaignContactDto.class);
        verify(campaignContactService).saveCampaignContact(captor.capture());

        CampaignContactDto saved = captor.getValue();
        assertThat(saved.isReplyReceived()).isTrue();
        assertThat(saved.getEmailData().get(0).getEmailDeliveryStatus())
                .isEqualTo(EmailDeliveryStatus.REPLIED);
    }

    @Test
    void syncReplies_noReply_statusRemainsSet() {
        when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                eq(MailBoxType.OUTLOOK), eq(EmailDeliveryStatus.SENT), any(), any()))
                .thenReturn(singlePage(contactWith(sentOutlookEmailData())));
        when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
        when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
        when(azureAuthClient.fetchAccessTokenAndRefreshTokenFromRefreshToken(REFRESH_TOKEN))
                .thenReturn(token());
        when(azureEmailClient.getConversationById(AZURE_ID, CONVERSATION_ID, ACCESS_TOKEN))
                .thenReturn(conversation(azureMessage(SENDER, RECIPIENT)));  // only 1 message
        when(campaignContactService.saveCampaignContact(any())).thenAnswer(i -> i.getArgument(0));

        service.syncReplies();

        ArgumentCaptor<CampaignContactDto> captor = ArgumentCaptor.forClass(CampaignContactDto.class);
        verify(campaignContactService).saveCampaignContact(captor.capture());

        assertThat(captor.getValue().isReplyReceived()).isFalse();
        assertThat(captor.getValue().getEmailData().get(0).getEmailDeliveryStatus())
                .isEqualTo(EmailDeliveryStatus.SENT);
    }

    @Test
    void syncReplies_emptyConversation_noSave() {
        AzureConversationResponse emptyConversation = new AzureConversationResponse();
        emptyConversation.setValue(List.of());

        when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                eq(MailBoxType.OUTLOOK), eq(EmailDeliveryStatus.SENT), any(), any()))
                .thenReturn(singlePage(contactWith(sentOutlookEmailData())));
        when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
        when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
        when(azureAuthClient.fetchAccessTokenAndRefreshTokenFromRefreshToken(REFRESH_TOKEN))
                .thenReturn(token());
        when(azureEmailClient.getConversationById(AZURE_ID, CONVERSATION_ID, ACCESS_TOKEN))
                .thenReturn(emptyConversation);

        service.syncReplies();

        verify(campaignContactService, never()).saveCampaignContact(any());
    }

    @Test
    void syncReplies_messagesStoredOnEmailData() {
        when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                eq(MailBoxType.OUTLOOK), eq(EmailDeliveryStatus.SENT), any(), any()))
                .thenReturn(singlePage(contactWith(sentOutlookEmailData())));
        when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
        when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
        when(azureAuthClient.fetchAccessTokenAndRefreshTokenFromRefreshToken(REFRESH_TOKEN))
                .thenReturn(token());
        when(azureEmailClient.getConversationById(AZURE_ID, CONVERSATION_ID, ACCESS_TOKEN))
                .thenReturn(conversation(
                        azureMessage(SENDER, RECIPIENT),
                        azureMessage(RECIPIENT, SENDER)
                ));
        when(campaignContactService.saveCampaignContact(any())).thenAnswer(i -> i.getArgument(0));

        service.syncReplies();

        ArgumentCaptor<CampaignContactDto> captor = ArgumentCaptor.forClass(CampaignContactDto.class);
        verify(campaignContactService).saveCampaignContact(captor.capture());

        List<MessageDto> messages = captor.getValue().getEmailData().get(0).getMessageDtos();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getFromAddress()).isEqualTo(SENDER);
        assertThat(messages.get(0).getToAddresses()).containsExactly(RECIPIENT);
        assertThat(messages.get(0).getSubject()).isEqualTo("Test Subject");
        assertThat(messages.get(0).getBody()).isEqualTo("Hello");
    }

    @Test
    void syncReplies_nullSender_handledGracefully() {
        AzureGraphMessageResponse msgWithNullSender = azureMessage(SENDER, RECIPIENT);
        msgWithNullSender.setSender(null);

        when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                eq(MailBoxType.OUTLOOK), eq(EmailDeliveryStatus.SENT), any(), any()))
                .thenReturn(singlePage(contactWith(sentOutlookEmailData())));
        when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
        when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
        when(azureAuthClient.fetchAccessTokenAndRefreshTokenFromRefreshToken(REFRESH_TOKEN))
                .thenReturn(token());
        when(azureEmailClient.getConversationById(AZURE_ID, CONVERSATION_ID, ACCESS_TOKEN))
                .thenReturn(conversation(msgWithNullSender));
        when(campaignContactService.saveCampaignContact(any())).thenAnswer(i -> i.getArgument(0));

        service.syncReplies();

        ArgumentCaptor<CampaignContactDto> captor = ArgumentCaptor.forClass(CampaignContactDto.class);
        verify(campaignContactService).saveCampaignContact(captor.capture());

        // fromAddress should be null, not throw
        assertThat(captor.getValue().getEmailData().get(0).getMessageDtos().get(0).getFromAddress())
                .isNull();
    }

    @Test
    void syncReplies_usesAzureIdFromMailboxMetadata() {
        when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                eq(MailBoxType.OUTLOOK), eq(EmailDeliveryStatus.SENT), any(), any()))
                .thenReturn(singlePage(contactWith(sentOutlookEmailData())));
        when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
        when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
        when(azureAuthClient.fetchAccessTokenAndRefreshTokenFromRefreshToken(REFRESH_TOKEN))
                .thenReturn(token());
        when(azureEmailClient.getConversationById(any(), any(), any()))
                .thenReturn(conversation(azureMessage(SENDER, RECIPIENT)));
        when(campaignContactService.saveCampaignContact(any())).thenAnswer(i -> i.getArgument(0));

        service.syncReplies();

        // verify that the azure user ID from mailbox metadata is passed to the client
        verify(azureEmailClient).getConversationById(eq(AZURE_ID), eq(CONVERSATION_ID), eq(ACCESS_TOKEN));
    }
}
