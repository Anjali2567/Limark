package ai.leadplus.application.campaigncontacts;

import ai.leadplus.application.campaigns.CampaignService;
import ai.leadplus.application.mailboxes.MailboxDto;
import ai.leadplus.application.mailboxes.MailboxService;
import ai.leadplus.domain.mailboxes.MailBoxType;
import ai.leadplus.infrastructure.azure.auth.AzureAuthClient;
import ai.leadplus.infrastructure.azure.auth.AzureRefreshTokenResponse;
import ai.leadplus.infrastructure.azure.graph.emails.AzureConversationResponse;
import ai.leadplus.infrastructure.azure.graph.emails.AzureEmailClient;
import ai.leadplus.infrastructure.azure.graph.emails.AzureGraphMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AzureReplySyncService extends AbstractReplySyncService<AzureRefreshTokenResponse> {

    private final AzureEmailClient azureEmailClient;
    private final AzureAuthClient azureAuthClient;

    public AzureReplySyncService(CampaignContactService campaignContactService,
                                 CampaignService campaignService,
                                 MailboxService mailboxService,
                                 ApplicationEventPublisher eventPublisher,
                                 AzureEmailClient azureEmailClient,
                                 AzureAuthClient azureAuthClient) {
        super(campaignContactService, campaignService, mailboxService, eventPublisher);
        this.azureEmailClient = azureEmailClient;
        this.azureAuthClient = azureAuthClient;
    }

    @Async
    @Override
    public void syncReplies() {
        super.syncReplies();
    }

    @Override
    protected MailBoxType getMailBoxType() {
        return MailBoxType.OUTLOOK;
    }

    @Override
    protected AzureRefreshTokenResponse refreshToken(MailboxDto mailbox) {
        return azureAuthClient.fetchAccessTokenAndRefreshTokenFromRefreshToken(
                mailbox.getMetaData().getRefreshToken()
        );
    }

    @Override
    protected List<MessageDto> fetchMessages(EmailDataDto emailData, MailboxDto mailbox, AzureRefreshTokenResponse token) {
        AzureConversationResponse conversation = azureEmailClient.getConversationById(
                mailbox.getMetaData().getAzureId(),
                emailData.getConversationId(),
                token.getAccess_token()
        );

        if (CollectionUtils.isEmpty(conversation.getValue())) {
            return List.of();
        }

        List<MessageDto> messages = new ArrayList<>();
        for (AzureGraphMessageResponse msg : conversation.getValue()) {
            messages.add(MessageDto.builder()
                    .fromAddress(formatAddress(
                            msg.getSender() != null
                                    ? msg.getSender().getEmailAddress().getAddress()
                                    : null
                    ))
                    .toAddresses(extractRecipientAddresses(msg.getToRecipients()))
                    .ccAddresses(extractRecipientAddresses(msg.getCcRecipients()))
                    .bccAddresses(extractRecipientAddresses(msg.getBccRecipients()))
                    .subject(msg.getSubject())
                    .body(msg.getBody() != null ? msg.getBody().getContent() : null)
                    .build());
        }
        return messages;
    }

    private List<String> extractRecipientAddresses(List<AzureGraphMessageResponse.Recipient> recipients) {
        if (recipients == null || recipients.isEmpty()) {
            return List.of();
        }

        List<String> emails = new ArrayList<>();
        for (AzureGraphMessageResponse.Recipient recipient : recipients) {
            if (recipient == null || recipient.getEmailAddress() == null) continue;

            String address = recipient.getEmailAddress().getAddress();
            if (StringUtils.hasText(address)) {
                emails.add(formatAddress(address));
            }
        }
        return emails;
    }

    private String formatAddress(String email) {
        return email != null ? email.trim().toLowerCase() : null;
    }
}
