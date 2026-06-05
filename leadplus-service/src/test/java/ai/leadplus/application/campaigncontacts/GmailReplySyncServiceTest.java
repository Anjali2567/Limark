package ai.leadplus.application.campaigncontacts;

import ai.leadplus.application.campaigns.CampaignDto;
import ai.leadplus.application.campaigns.CampaignService;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.mailboxes.MailboxDto;
import ai.leadplus.application.mailboxes.MailboxProviderConfigDto;
import ai.leadplus.application.mailboxes.MailboxService;
import ai.leadplus.domain.mailboxes.MailBoxType;
import ai.leadplus.infrastructure.google.auth.GoogleAuthClient;
import ai.leadplus.infrastructure.google.auth.GoogleTokenResponse;
import ai.leadplus.infrastructure.google.gmail.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GmailReplySyncServiceTest {

    @Mock private CampaignContactService campaignContactService;
    @Mock private CampaignService campaignService;
    @Mock private MailboxService mailboxService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private GmailClient gmailClient;
    @Mock private GoogleAuthClient googleAuthClient;

    private GmailReplySyncService service;

    private static final Long CAMPAIGN_ID = 1L;
    private static final Long MAILBOX_ID = 10L;
    private static final Long CONTACT_ID = 100L;
    private static final String THREAD_ID = "thread-abc";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String SENDER_EMAIL = "sender@example.com";
    private static final String RECIPIENT_EMAIL = "recipient@example.com";

    @BeforeEach
    void setUp() {
        service = new GmailReplySyncService(
                campaignContactService, campaignService, mailboxService,
                eventPublisher, gmailClient, googleAuthClient
        );
    }

    // --- Helpers ---

    private CampaignContactDto contact(EmailDataDto... emailData) {
        CampaignContactDto dto = new CampaignContactDto();
        dto.setId(CONTACT_ID);
        dto.setCampaignId(CAMPAIGN_ID);
        dto.setReplyReceived(false);
        dto.setEmailData(List.of(emailData));
        return dto;
    }

    private EmailDataDto sentGmailEmailData() {
        EmailDataDto dto = new EmailDataDto();
        dto.setEmailPlatform(MailBoxType.GMAIL);
        dto.setEmailDeliveryStatus(EmailDeliveryStatus.SENT);
        dto.setConversationId(THREAD_ID);
        return dto;
    }

    private CampaignDto campaign() {
        return CampaignDto.builder()
                .id(CAMPAIGN_ID)
                .sendingMailboxId(MAILBOX_ID)
                .build();
    }

    private MailboxDto mailbox() {
        return MailboxDto.builder()
                .id(MAILBOX_ID)
                .metaData(MailboxProviderConfigDto.builder()
                        .refreshToken(REFRESH_TOKEN)
                        .build())
                .build();
    }

    private GoogleTokenResponse token() {
        return GoogleTokenResponse.builder()
                .access_token(ACCESS_TOKEN)
                .build();
    }

    private GmailThreadResponse threadWith(GmailMessage... messages) {
        GmailThreadResponse thread = new GmailThreadResponse();
        thread.setMessages(List.of(messages));
        return thread;
    }

    private GmailMessage gmailMessage(String from, String to) {
        GmailModels.GmailHeader fromHeader = new GmailModels.GmailHeader();
        fromHeader.setName("From");
        fromHeader.setValue(from);

        GmailModels.GmailHeader toHeader = new GmailModels.GmailHeader();
        toHeader.setName("To");
        toHeader.setValue(to);

        GmailModels.GmailMessagePayload payload = new GmailModels.GmailMessagePayload();
        payload.setHeaders(List.of(fromHeader, toHeader));
        payload.setMimeType("text/plain");
        payload.setParts(List.of());

        GmailMessage msg = new GmailMessage();
        msg.setPayload(payload);
        return msg;
    }

    private Page<CampaignContactDto> pageOf(CampaignContactDto... contacts) {
        return new PageImpl<>(List.of(contacts), PageRequest.of(0, 100), contacts.length);
    }

    private Page<CampaignContactDto> emptyPage() {
        return new PageImpl<>(List.of(), PageRequest.of(0, 100), 0);
    }

    // -------------------------------------------------------------------------
    // 1. Early exit conditions
    // -------------------------------------------------------------------------

    @Nested
    class EarlyExit {

        @Test
        void noContacts_doesNothing() {
            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(emptyPage());

            service.syncReplies();

            verify(campaignContactService, never()).saveCampaignContact(any());
        }

        @Test
        void contactWithNoEmailData_isSkipped() {
            CampaignContactDto contact = new CampaignContactDto();
            contact.setId(CONTACT_ID);
            contact.setCampaignId(CAMPAIGN_ID);
            contact.setEmailData(List.of());

            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(contact));

            service.syncReplies();

            verify(campaignService, never()).getCampaignById(any());
            verify(campaignContactService, never()).saveCampaignContact(any());
        }
    }

    // -------------------------------------------------------------------------
    // 2. Campaign / Mailbox / Token guard rails
    // -------------------------------------------------------------------------

    @Nested
    class GuardRails {

        @Test
        void campaignNotFound_contactSkipped_othersStillProcessed() {
            CampaignContactDto badContact = contact(sentGmailEmailData());
            badContact.setCampaignId(99L);

            CampaignContactDto goodContact = contact(sentGmailEmailData());

            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(badContact, goodContact));
            when(campaignService.getCampaignById(99L))
                    .thenThrow(new ResourceNotFoundException("Campaign not found with id: 99"));
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
            when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
            when(googleAuthClient.fetchTokensFromRefreshToken(REFRESH_TOKEN)).thenReturn(token());
            when(gmailClient.getThreadById(eq(THREAD_ID), eq(ACCESS_TOKEN)))
                    .thenReturn(threadWith(
                            gmailMessage(SENDER_EMAIL, RECIPIENT_EMAIL),
                            gmailMessage(RECIPIENT_EMAIL, SENDER_EMAIL)
                    ));
            when(campaignContactService.saveCampaignContact(any())).thenAnswer(i -> i.getArgument(0));

            service.syncReplies();

            verify(campaignContactService, times(1)).saveCampaignContact(any());
        }

        @Test
        void campaignHasNoMailboxId_contactSkipped() {
            CampaignDto campaignNoMailbox = CampaignDto.builder()
                    .id(CAMPAIGN_ID)
                    .sendingMailboxId(null)
                    .build();

            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(contact(sentGmailEmailData())));
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaignNoMailbox);

            service.syncReplies();

            verify(mailboxService, never()).getMailboxById(any());
            verify(campaignContactService, never()).saveCampaignContact(any());
        }

        @Test
        void mailboxNotFound_contactSkipped() {
            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(contact(sentGmailEmailData())));
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
            when(mailboxService.getMailboxById(MAILBOX_ID))
                    .thenThrow(new ResourceNotFoundException("Mailbox not found"));

            service.syncReplies();

            verify(googleAuthClient, never()).fetchTokensFromRefreshToken(any());
            verify(campaignContactService, never()).saveCampaignContact(any());
        }

        @Test
        void mailboxHasNoMetadata_contactSkipped() {
            MailboxDto noMetaMailbox = MailboxDto.builder().id(MAILBOX_ID).metaData(null).build();

            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(contact(sentGmailEmailData())));
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
            when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(noMetaMailbox);

            service.syncReplies();

            verify(googleAuthClient, never()).fetchTokensFromRefreshToken(any());
            verify(campaignContactService, never()).saveCampaignContact(any());
        }

        @Test
        void tokenRefreshFails_contactSkipped() {
            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(contact(sentGmailEmailData())));
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
            when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
            when(googleAuthClient.fetchTokensFromRefreshToken(REFRESH_TOKEN))
                    .thenThrow(new RuntimeException("Token refresh failed"));

            service.syncReplies();

            verify(gmailClient, never()).getThreadById(any(), any());
            verify(campaignContactService, never()).saveCampaignContact(any());
        }
    }

    // -------------------------------------------------------------------------
    // 3. EmailData filtering
    // -------------------------------------------------------------------------

    @Nested
    class EmailDataFiltering {

        @Test
        void emailDataWithWrongPlatform_skipped() {
            EmailDataDto outlookData = sentGmailEmailData();
            outlookData.setEmailPlatform(MailBoxType.OUTLOOK);

            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(contact(outlookData)));
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
            when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
            when(googleAuthClient.fetchTokensFromRefreshToken(REFRESH_TOKEN)).thenReturn(token());

            service.syncReplies();

            verify(gmailClient, never()).getThreadById(any(), any());
            verify(campaignContactService, never()).saveCampaignContact(any());
        }

        @Test
        void emailDataWithWrongStatus_skipped() {
            EmailDataDto repliedData = sentGmailEmailData();
            repliedData.setEmailDeliveryStatus(EmailDeliveryStatus.REPLIED);

            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(contact(repliedData)));
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
            when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
            when(googleAuthClient.fetchTokensFromRefreshToken(REFRESH_TOKEN)).thenReturn(token());

            service.syncReplies();

            verify(gmailClient, never()).getThreadById(any(), any());
            verify(campaignContactService, never()).saveCampaignContact(any());
        }

        @Test
        void emailDataWithBlankConversationId_skipped() {
            EmailDataDto noThread = sentGmailEmailData();
            noThread.setConversationId(null);

            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(contact(noThread)));
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
            when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
            when(googleAuthClient.fetchTokensFromRefreshToken(REFRESH_TOKEN)).thenReturn(token());

            service.syncReplies();

            verify(gmailClient, never()).getThreadById(any(), any());
            verify(campaignContactService, never()).saveCampaignContact(any());
        }

        @Test
        void fetchMessagesThrowsException_contactNotSaved() {
            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(contact(sentGmailEmailData())));
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
            when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
            when(googleAuthClient.fetchTokensFromRefreshToken(REFRESH_TOKEN)).thenReturn(token());
            when(gmailClient.getThreadById(any(), any()))
                    .thenThrow(new RuntimeException("Gmail API error"));

            service.syncReplies();

            verify(campaignContactService, never()).saveCampaignContact(any());
        }

        @Test
        void threadHasNoMessages_contactNotSaved() {
            GmailThreadResponse emptyThread = new GmailThreadResponse();
            emptyThread.setMessages(List.of());

            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(contact(sentGmailEmailData())));
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
            when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
            when(googleAuthClient.fetchTokensFromRefreshToken(REFRESH_TOKEN)).thenReturn(token());
            when(gmailClient.getThreadById(eq(THREAD_ID), eq(ACCESS_TOKEN))).thenReturn(emptyThread);

            service.syncReplies();

            verify(campaignContactService, never()).saveCampaignContact(any());
        }
    }

    // -------------------------------------------------------------------------
    // 4. Reply detection
    // -------------------------------------------------------------------------

    @Nested
    class ReplyDetection {

        @Test
        void singleMessageInThread_notReplied_statusRemainsUnchanged() {
            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(contact(sentGmailEmailData())));
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
            when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
            when(googleAuthClient.fetchTokensFromRefreshToken(REFRESH_TOKEN)).thenReturn(token());
            when(gmailClient.getThreadById(eq(THREAD_ID), eq(ACCESS_TOKEN)))
                    .thenReturn(threadWith(gmailMessage(SENDER_EMAIL, RECIPIENT_EMAIL)));
            when(campaignContactService.saveCampaignContact(any())).thenAnswer(i -> i.getArgument(0));

            service.syncReplies();

            ArgumentCaptor<CampaignContactDto> captor = ArgumentCaptor.forClass(CampaignContactDto.class);
            verify(campaignContactService).saveCampaignContact(captor.capture());
            assertThat(captor.getValue().getEmailData().getFirst().getEmailDeliveryStatus())
                    .isEqualTo(EmailDeliveryStatus.SENT);
            assertThat(captor.getValue().isReplyReceived()).isFalse();
        }

        @Test
        void secondMessageHasSenderInTo_detectedAsReplied() {
            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(contact(sentGmailEmailData())));
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
            when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
            when(googleAuthClient.fetchTokensFromRefreshToken(REFRESH_TOKEN)).thenReturn(token());
            when(gmailClient.getThreadById(eq(THREAD_ID), eq(ACCESS_TOKEN)))
                    .thenReturn(threadWith(
                            gmailMessage(SENDER_EMAIL, RECIPIENT_EMAIL),
                            gmailMessage(RECIPIENT_EMAIL, SENDER_EMAIL)
                    ));
            when(campaignContactService.saveCampaignContact(any())).thenAnswer(i -> i.getArgument(0));

            service.syncReplies();

            ArgumentCaptor<CampaignContactDto> captor = ArgumentCaptor.forClass(CampaignContactDto.class);
            verify(campaignContactService).saveCampaignContact(captor.capture());
            assertThat(captor.getValue().getEmailData().getFirst().getEmailDeliveryStatus())
                    .isEqualTo(EmailDeliveryStatus.REPLIED);
            assertThat(captor.getValue().isReplyReceived()).isTrue();
        }

        @Test
        void replyDetectedWithDisplayNameFormat_emailExtractedCorrectly() {
            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(contact(sentGmailEmailData())));
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
            when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
            when(googleAuthClient.fetchTokensFromRefreshToken(REFRESH_TOKEN)).thenReturn(token());
            when(gmailClient.getThreadById(eq(THREAD_ID), eq(ACCESS_TOKEN)))
                    .thenReturn(threadWith(
                            gmailMessage("Sender Name <" + SENDER_EMAIL + ">", RECIPIENT_EMAIL),
                            gmailMessage(RECIPIENT_EMAIL, "Sender Name <" + SENDER_EMAIL + ">")
                    ));
            when(campaignContactService.saveCampaignContact(any())).thenAnswer(i -> i.getArgument(0));

            service.syncReplies();

            ArgumentCaptor<CampaignContactDto> captor = ArgumentCaptor.forClass(CampaignContactDto.class);
            verify(campaignContactService).saveCampaignContact(captor.capture());
            assertThat(captor.getValue().getEmailData().getFirst().getEmailDeliveryStatus())
                    .isEqualTo(EmailDeliveryStatus.REPLIED);
        }

        @Test
        void replyEmailCaseInsensitive_detectedCorrectly() {
            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(contact(sentGmailEmailData())));
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
            when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
            when(googleAuthClient.fetchTokensFromRefreshToken(REFRESH_TOKEN)).thenReturn(token());
            when(gmailClient.getThreadById(eq(THREAD_ID), eq(ACCESS_TOKEN)))
                    .thenReturn(threadWith(
                            gmailMessage("SENDER@EXAMPLE.COM", RECIPIENT_EMAIL),
                            gmailMessage(RECIPIENT_EMAIL, "sender@EXAMPLE.com")
                    ));
            when(campaignContactService.saveCampaignContact(any())).thenAnswer(i -> i.getArgument(0));

            service.syncReplies();

            ArgumentCaptor<CampaignContactDto> captor = ArgumentCaptor.forClass(CampaignContactDto.class);
            verify(campaignContactService).saveCampaignContact(captor.capture());
            assertThat(captor.getValue().getEmailData().getFirst().getEmailDeliveryStatus())
                    .isEqualTo(EmailDeliveryStatus.REPLIED);
        }

        @Test
        void secondMessageDoesNotHaveSenderInTo_notReplied() {
            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(contact(sentGmailEmailData())));
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
            when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
            when(googleAuthClient.fetchTokensFromRefreshToken(REFRESH_TOKEN)).thenReturn(token());
            when(gmailClient.getThreadById(eq(THREAD_ID), eq(ACCESS_TOKEN)))
                    .thenReturn(threadWith(
                            gmailMessage(SENDER_EMAIL, RECIPIENT_EMAIL),
                            gmailMessage(RECIPIENT_EMAIL, "someoneelse@example.com")
                    ));
            when(campaignContactService.saveCampaignContact(any())).thenAnswer(i -> i.getArgument(0));

            service.syncReplies();

            ArgumentCaptor<CampaignContactDto> captor = ArgumentCaptor.forClass(CampaignContactDto.class);
            verify(campaignContactService).saveCampaignContact(captor.capture());
            assertThat(captor.getValue().getEmailData().getFirst().getEmailDeliveryStatus())
                    .isEqualTo(EmailDeliveryStatus.SENT);
        }

        @Test
        void contactAlreadyReplied_replyReceivedNotSetAgain() {
            CampaignContactDto alreadyReplied = contact(sentGmailEmailData());
            alreadyReplied.setReplyReceived(true);

            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(alreadyReplied));
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
            when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
            when(googleAuthClient.fetchTokensFromRefreshToken(REFRESH_TOKEN)).thenReturn(token());
            when(gmailClient.getThreadById(eq(THREAD_ID), eq(ACCESS_TOKEN)))
                    .thenReturn(threadWith(
                            gmailMessage(SENDER_EMAIL, RECIPIENT_EMAIL),
                            gmailMessage(RECIPIENT_EMAIL, SENDER_EMAIL)
                    ));
            when(campaignContactService.saveCampaignContact(any())).thenAnswer(i -> i.getArgument(0));

            service.syncReplies();

            ArgumentCaptor<CampaignContactDto> captor = ArgumentCaptor.forClass(CampaignContactDto.class);
            verify(campaignContactService).saveCampaignContact(captor.capture());
            assertThat(captor.getValue().getEmailData().getFirst().getEmailDeliveryStatus())
                    .isEqualTo(EmailDeliveryStatus.REPLIED);
            // replyReceived was already true — stays true (no double-toggle)
            assertThat(captor.getValue().isReplyReceived()).isTrue();
        }
    }

    // -------------------------------------------------------------------------
    // 5. Caching
    // -------------------------------------------------------------------------

    @Nested
    class Caching {

        @Test
        void multipleContactsSameCampaign_campaignAndTokenFetchedOnce() {
            CampaignContactDto c1 = contact(sentGmailEmailData());
            CampaignContactDto c2 = contact(sentGmailEmailData());
            c2.setId(CONTACT_ID + 1);

            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(c1, c2));
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
            when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
            when(googleAuthClient.fetchTokensFromRefreshToken(REFRESH_TOKEN)).thenReturn(token());
            when(gmailClient.getThreadById(any(), any()))
                    .thenReturn(threadWith(gmailMessage(SENDER_EMAIL, RECIPIENT_EMAIL)));
            when(campaignContactService.saveCampaignContact(any())).thenAnswer(i -> i.getArgument(0));

            service.syncReplies();

            verify(campaignService, times(1)).getCampaignById(CAMPAIGN_ID);
            verify(mailboxService, times(1)).getMailboxById(MAILBOX_ID);
            verify(googleAuthClient, times(1)).fetchTokensFromRefreshToken(REFRESH_TOKEN);
        }
    }

    // -------------------------------------------------------------------------
    // 6. Pagination
    // -------------------------------------------------------------------------

    @Nested
    class Pagination {

        @Test
        @SuppressWarnings("unchecked")
        void multiplePages_allContactsProcessed() {
            CampaignContactDto contact1 = contact(sentGmailEmailData());
            CampaignContactDto contact2 = contact(sentGmailEmailData());
            contact2.setId(CONTACT_ID + 1);

            Page<CampaignContactDto> page1 = mock(Page.class);
            when(page1.getContent()).thenReturn(List.of(contact1));
            when(page1.hasNext()).thenReturn(true);

            Page<CampaignContactDto> page2 = mock(Page.class);
            when(page2.getContent()).thenReturn(List.of(contact2));
            when(page2.hasNext()).thenReturn(false);

            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(page1, page2);
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
            when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
            when(googleAuthClient.fetchTokensFromRefreshToken(REFRESH_TOKEN)).thenReturn(token());
            when(gmailClient.getThreadById(any(), any()))
                    .thenReturn(threadWith(gmailMessage(SENDER_EMAIL, RECIPIENT_EMAIL)));
            when(campaignContactService.saveCampaignContact(any())).thenAnswer(i -> i.getArgument(0));

            service.syncReplies();

            verify(campaignContactService, times(2)).saveCampaignContact(any());
        }
    }

    // -------------------------------------------------------------------------
    // 7. Message storage
    // -------------------------------------------------------------------------

    @Nested
    class MessageStorage {

        @Test
        void messagesStoredOnEmailData_withCorrectHeaderFields() {
            GmailModels.GmailHeader subjectHeader = new GmailModels.GmailHeader();
            subjectHeader.setName("Subject");
            subjectHeader.setValue("Hello there");

            GmailModels.GmailHeader fromHeader = new GmailModels.GmailHeader();
            fromHeader.setName("From");
            fromHeader.setValue(SENDER_EMAIL);

            GmailModels.GmailHeader toHeader = new GmailModels.GmailHeader();
            toHeader.setName("To");
            toHeader.setValue(RECIPIENT_EMAIL);

            GmailModels.GmailMessagePayload payload = new GmailModels.GmailMessagePayload();
            payload.setHeaders(List.of(fromHeader, toHeader, subjectHeader));
            payload.setMimeType("text/plain");
            payload.setParts(List.of());

            GmailMessage msg = new GmailMessage();
            msg.setPayload(payload);

            when(campaignContactService.getCampaignContactsByPlatformAndStatus(
                    eq(MailBoxType.GMAIL), eq(EmailDeliveryStatus.SENT), any(), any()))
                    .thenReturn(pageOf(contact(sentGmailEmailData())));
            when(campaignService.getCampaignById(CAMPAIGN_ID)).thenReturn(campaign());
            when(mailboxService.getMailboxById(MAILBOX_ID)).thenReturn(mailbox());
            when(googleAuthClient.fetchTokensFromRefreshToken(REFRESH_TOKEN)).thenReturn(token());
            when(gmailClient.getThreadById(eq(THREAD_ID), eq(ACCESS_TOKEN)))
                    .thenReturn(threadWith(msg));
            when(campaignContactService.saveCampaignContact(any())).thenAnswer(i -> i.getArgument(0));

            service.syncReplies();

            ArgumentCaptor<CampaignContactDto> captor = ArgumentCaptor.forClass(CampaignContactDto.class);
            verify(campaignContactService).saveCampaignContact(captor.capture());

            List<MessageDto> messages = captor.getValue().getEmailData().getFirst().getMessageDtos();
            assertThat(messages).hasSize(1);
            assertThat(messages.getFirst().getFromAddress()).isEqualTo(SENDER_EMAIL);
            assertThat(messages.getFirst().getToAddresses()).containsExactly(RECIPIENT_EMAIL);
            assertThat(messages.getFirst().getSubject()).isEqualTo("Hello there");
        }
    }
}
