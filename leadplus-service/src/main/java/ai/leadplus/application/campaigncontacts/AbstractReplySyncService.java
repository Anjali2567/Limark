package ai.leadplus.application.campaigncontacts;

import ai.leadplus.application.campaignorchestrator.CampaignEmailRepliedEvent;
import ai.leadplus.application.campaigns.CampaignDto;
import ai.leadplus.application.campaigns.CampaignService;
import ai.leadplus.application.mailboxes.MailboxDto;
import ai.leadplus.application.mailboxes.MailboxService;
import ai.leadplus.domain.mailboxes.MailBoxType;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
public abstract class AbstractReplySyncService<T> {

    private static final int BATCH_SIZE = 100;

    private final CampaignContactService campaignContactService;
    private final CampaignService campaignService;
    private final MailboxService mailboxService;
    private final ApplicationEventPublisher eventPublisher;

    protected AbstractReplySyncService(CampaignContactService campaignContactService,
                                       CampaignService campaignService,
                                       MailboxService mailboxService,
                                       ApplicationEventPublisher eventPublisher) {
        this.campaignContactService = campaignContactService;
        this.campaignService = campaignService;
        this.mailboxService = mailboxService;
        this.eventPublisher = eventPublisher;
    }

    protected abstract MailBoxType getMailBoxType();

    protected abstract T refreshToken(MailboxDto mailbox);

    protected abstract List<MessageDto> fetchMessages(EmailDataDto emailData, MailboxDto mailbox, T token);

    public void syncReplies() {

        LocalDateTime since = LocalDateTime.now(ZoneOffset.UTC).minusMonths(1);
        Map<Long, CampaignDto> campaignCache = new HashMap<>();
        Map<Long, MailboxDto> mailboxCache = new HashMap<>();
        Map<Long, T> tokenCache = new HashMap<>();

        int page = 0;
        Page<CampaignContactDto> contactPage;

        do {
            contactPage = campaignContactService.getCampaignContactsByPlatformAndStatus(
                    getMailBoxType(),
                    EmailDeliveryStatus.SENT,
                    since,
                    PageRequest.of(page, BATCH_SIZE)
            );

            for (CampaignContactDto contact : contactPage.getContent()) {
                processContact(contact, campaignCache, mailboxCache, tokenCache);
            }

            page++;
        } while (contactPage.hasNext());
    }

    private void processContact(CampaignContactDto contact,
                                Map<Long, CampaignDto> campaignCache,
                                Map<Long, MailboxDto> mailboxCache,
                                Map<Long, T> tokenCache) {

        if (CollectionUtils.isEmpty(contact.getEmailData())) {
            return;
        }

        boolean contactUpdated = false;
        boolean replyDetected = false;
        boolean alreadyReplied = contact.isReplyReceived();

        CampaignDto campaign;
        try {
            campaign = campaignCache.computeIfAbsent(
                    contact.getCampaignId(),
                    campaignService::getCampaignById
            );
        } catch (Exception e) {
            log.warn("Skipping contact {} - campaign {} not found", contact.getId(), contact.getCampaignId());
            return;
        }

        if (campaign == null || campaign.getSendingMailboxId() == null) {
            return;
        }

        MailboxDto mailbox;
        try {
            mailbox = mailboxCache.computeIfAbsent(
                    campaign.getSendingMailboxId(),
                    mailboxService::getMailboxById
            );
        } catch (Exception e) {
            log.warn("Skipping contact {} - mailbox {} not found", contact.getId(), campaign.getSendingMailboxId());
            return;
        }

        if (mailbox == null || mailbox.getMetaData() == null) {
            return;
        }

        T token;
        try {
            token = tokenCache.computeIfAbsent(
                    mailbox.getId(),
                    id -> refreshToken(mailbox)
            );
        } catch (Exception e) {
            log.error("Skipping contact {} - failed to refresh token for mailbox {}", contact.getId(), mailbox.getId(), e);
            return;
        }

        for (EmailDataDto emailData : contact.getEmailData()) {

            if (!Objects.equals(emailData.getEmailPlatform(), getMailBoxType()) ||
                    !Objects.equals(emailData.getEmailDeliveryStatus(), EmailDeliveryStatus.SENT) ||
                    !StringUtils.hasText(emailData.getConversationId())) {
                continue;
            }

            try {
                List<MessageDto> messages = fetchMessages(emailData, mailbox, token);

                if (CollectionUtils.isEmpty(messages)) {
                    continue;
                }

                emailData.setMessageDtos(messages);
                contactUpdated = true;

                if (isReplied(messages)) {
                    emailData.setEmailDeliveryStatus(EmailDeliveryStatus.REPLIED);

                    if (!alreadyReplied) {
                        contact.setReplyReceived(true);
                        replyDetected = true;
                    }
                }

            } catch (Exception e) {
                log.error(
                        "Failed to sync {} replies for CampaignContact {} (conversationId={})",
                        getMailBoxType(),
                        contact.getId(),
                        emailData.getConversationId(),
                        e
                );
            }
        }

        if (contactUpdated) {
            CampaignContactDto savedContact =
                    campaignContactService.saveCampaignContact(contact);

            if (replyDetected) {
                CampaignEmailRepliedEvent event =
                        new CampaignEmailRepliedEvent(this, savedContact);
//                eventPublisher.publishEvent(event);
            }
        }
    }

    private boolean isReplied(List<MessageDto> messages) {

        if (messages == null || messages.size() < 2) {
            return false;
        }

        MessageDto first = messages.getFirst();
        String originalSenderEmail = extractEmail(first.getFromAddress());

        if (originalSenderEmail == null) {
            return false;
        }

        for (int i = 1; i < messages.size(); i++) {
            MessageDto msg = messages.get(i);

            if (CollectionUtils.isEmpty(msg.getToAddresses())) {
                continue;
            }

            for (String to : msg.getToAddresses()) {
                String toEmail = extractEmail(to);
                if (originalSenderEmail.equalsIgnoreCase(toEmail)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected static String extractEmail(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        try {
            InternetAddress internetAddress = new InternetAddress(address);
            return internetAddress.getAddress() != null
                    ? internetAddress.getAddress().toLowerCase()
                    : null;
        } catch (AddressException e) {
            return address.trim().toLowerCase();
        }
    }
}
