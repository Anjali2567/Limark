package ai.leadplus.application.campaigncontacts;

import ai.leadplus.application.campaigns.CampaignService;
import ai.leadplus.application.mailboxes.MailboxDto;
import ai.leadplus.application.mailboxes.MailboxService;
import ai.leadplus.domain.mailboxes.MailBoxType;
import ai.leadplus.infrastructure.google.auth.GoogleAuthClient;
import ai.leadplus.infrastructure.google.auth.GoogleTokenResponse;
import ai.leadplus.infrastructure.google.gmail.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GmailReplySyncService extends AbstractReplySyncService<GoogleTokenResponse> {

    private final GmailClient gmailClient;
    private final GoogleAuthClient googleAuthClient;

    public GmailReplySyncService(CampaignContactService campaignContactService,
                                 CampaignService campaignService,
                                 MailboxService mailboxService,
                                 ApplicationEventPublisher eventPublisher,
                                 GmailClient gmailClient,
                                 GoogleAuthClient googleAuthClient) {
        super(campaignContactService, campaignService, mailboxService, eventPublisher);
        this.gmailClient = gmailClient;
        this.googleAuthClient = googleAuthClient;
    }

    @Async
    @Override
    public void syncReplies() {
        super.syncReplies();
    }

    @Override
    protected MailBoxType getMailBoxType() {
        return MailBoxType.GMAIL;
    }

    @Override
    protected GoogleTokenResponse refreshToken(MailboxDto mailbox) {
        return googleAuthClient.fetchTokensFromRefreshToken(mailbox.getMetaData().getRefreshToken());
    }

    @Override
    protected List<MessageDto> fetchMessages(EmailDataDto emailData, MailboxDto mailbox, GoogleTokenResponse token) {
        GmailThreadResponse thread = gmailClient.getThreadById(
                emailData.getConversationId(),
                token.getAccess_token()
        );

        if (thread.getMessages() == null || thread.getMessages().isEmpty()) {
            return List.of();
        }

        List<MessageDto> messages = new ArrayList<>();
        for (GmailMessage message : thread.getMessages()) {
            messages.add(MessageDto.builder()
                    .fromAddress(extractHeader(message, "From"))
                    .toAddresses(extractHeaderList(message, "To"))
                    .ccAddresses(extractHeaderList(message, "Cc"))
                    .bccAddresses(extractHeaderList(message, "Bcc"))
                    .subject(extractHeader(message, "Subject"))
                    .body(GmailMessageParser.extractEmailBody(message.getPayload()))
                    .build());
        }
        return messages;
    }

    private String extractHeader(GmailMessage message, String headerName) {
        if (message == null || message.getPayload() == null || message.getPayload().getHeaders() == null) {
            return null;
        }
        return message.getPayload().getHeaders().stream()
                .filter(h -> h.getName().equalsIgnoreCase(headerName))
                .map(GmailModels.GmailHeader::getValue)
                .findFirst()
                .orElse(null);
    }

    private List<String> extractHeaderList(GmailMessage message, String headerName) {
        String headerValue = extractHeader(message, headerName);
        if (headerValue == null) return List.of();
        String[] addresses = headerValue.split(",");
        List<String> list = new ArrayList<>();
        for (String addr : addresses) list.add(addr.trim());
        return list;
    }
}
