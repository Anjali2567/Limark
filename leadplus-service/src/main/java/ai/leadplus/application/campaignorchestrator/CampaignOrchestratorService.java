package ai.leadplus.application.campaignorchestrator;

import ai.leadplus.application.aws.AwsSESService;
import ai.leadplus.application.azure.AzureEmailService;
import ai.leadplus.application.campaigncontacts.CampaignContactInfoDto;
import ai.leadplus.application.campaigncontacts.CampaignContactService;
import ai.leadplus.application.campaigncontacts.EmailDataDto;
import ai.leadplus.application.campaigncontacts.EmailDeliveryStatus;
import ai.leadplus.application.campaignemails.CampaignEmailDto;
import ai.leadplus.application.campaignemails.CampaignEmailService;
import ai.leadplus.application.campaigns.CampaignDto;
import ai.leadplus.application.campaigns.CampaignService;
import ai.leadplus.application.campaigns.SendingWindowService;
import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.common.utils.RecipientUtils;
import ai.leadplus.application.contactemails.ContactEmailDto;
import ai.leadplus.application.contactoutreachstatuses.ContactOutreachStatusService;
import ai.leadplus.application.exception.MailboxTokenExpiredException;
import ai.leadplus.application.google.GmailEmailService;
import ai.leadplus.application.mailboxes.MailboxDto;
import ai.leadplus.application.mailboxes.MailboxService;
import ai.leadplus.application.springmail.SpringMailService;
import ai.leadplus.application.tenants.TenantDto;
import ai.leadplus.application.tenants.TenantService;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.application.workspaces.WorkspaceDto;
import ai.leadplus.application.workspaces.WorkspaceService;
import ai.leadplus.domain.campaigncontacts.CampaignContactStatus;
import ai.leadplus.domain.campaignemails.CampaignEmailStatus;
import ai.leadplus.domain.campaigns.CampaignStatus;
import ai.leadplus.domain.tenantcontactmetadata.TenantContactMetadataRepository;
import ai.leadplus.infrastructure.azure.graph.emails.AzureGraphMessageResponse;
import ai.leadplus.infrastructure.google.gmail.GmailMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "campaign.orchestration.scheduler.enabled", havingValue = "true")
public class CampaignOrchestratorService {

    private final TenantService tenantService;
    private final CampaignService campaignService;
    private final CampaignContactService campaignContactService;
    private final CampaignEmailService campaignEmailService;
    private final ContactOutreachStatusService contactOutreachStatusService;
    private final MailMergeService mailMergeService;
    private final MailboxService mailboxService;
    private final AzureEmailService azureEmailService;
    private final GmailEmailService gmailEmailService;
    private final SpringMailService springMailService;
    private final AwsSESService awsSESService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final WorkspaceService workspaceService;
    private final SendingWindowService sendingWindowService;
    private final TenantContactMetadataRepository tenantContactMetadataRepository;

    @Value("${aws.cloudfront.url}")
    private String cloudfrontUrl;

    @Scheduled(cron = "${campaign.orchestration.scheduler.cron}")
    public void campaignEmailOrchestrator() {
        Optional<CampaignContactInfoDto> campaignContactInfoDto = campaignContactService.getTopCampaignContactToMail();
        if (campaignContactInfoDto.isPresent()) {
            log.info("Processing campaign contact for ID: {}", campaignContactInfoDto.get().getId());
            processCampaignContact(campaignContactInfoDto.get());
        }
    }

    /**
     * Processes a single campaign contact for email sending.
     * Guards are evaluated in order:
     * 1. Previous step ongoing    - delays by the current email's delay days until the prior step finishes.
     * 2. Contact eligibility      - delays by 1 day if the contact is unsubscribed, bounced, or ineligible.
     * 3. Mailbox token expired    - delays by 1 day if the mailbox token is expired until the user re-authenticates.
     * 4. Daily send limit         - delays by 1 day if the mailbox has reached its workspace limit for today.
     * 5. Sending window           - snaps nextSendAt to the next open slot if outside the configured window.
     * If all guards pass, the email is sent immediately.
     */
    private void processCampaignContact(CampaignContactInfoDto campaignContactInfoDto) {
        Long campaignId = campaignContactInfoDto.getCampaignId();
        int currentStep = campaignContactInfoDto.getCurrentStep();

        CampaignDto campaignDto = campaignService.getCampaignById(campaignId);
        Long tenantId = campaignDto.getTenantId();

        CampaignEmailDto campaignEmail = campaignEmailService.getCampaignEmailByCampaignIdAndStepNumber(campaignId, currentStep);

        // Check 1 — previous step still has unsent contacts; delay until it clears
        if ((currentStep > 1) && campaignContactService.isCurrentStepOngoing(campaignId, currentStep - 1)) {
            log.info("Delaying email send for campaign contact with ID: {} due to ongoing previous step.", campaignContactInfoDto.getId());
            delayNextSendAt(campaignContactInfoDto, campaignEmail.getDelayDays(), campaignDto);
            return;
        }

        // Check 2 — contact is not eligible (unsubscribed, bounced, etc.); delay by 1 day
        if (!contactOutreachStatusService.validateContactEligibility(tenantId, campaignContactInfoDto)) {
            log.info("Campaign contact with ID: {} is not eligible for email sending. Delaying next send date.", campaignContactInfoDto.getId());
            delayNextSendAt(campaignContactInfoDto, 1, campaignDto);
            return;
        }

        // Check 3 — mailbox token is expired; delay by 1 day until user re-authenticates
        MailboxDto mailbox = mailboxService.getMailboxById(campaignDto.getSendingMailboxId());
        if (mailbox.isTokenExpired()) {
            log.info("Mailbox with ID: {} has an expired token. Delaying email send for campaign contact with ID: {}.", mailbox.getId(), campaignContactInfoDto.getId());
            delayNextSendAt(campaignContactInfoDto, 1, campaignDto);
            return;
        }

        // Check 4 — mailbox has hit its daily send limit; delay by 1 day
        WorkspaceDto workspaceDto = workspaceService.getWorkspaceById(mailbox.getWorkspaceId());
        if (mailbox.getEmailsSentToday() >= workspaceDto.getDailySendLimit()) {
            log.info("Mailbox with ID: {} has reached its daily send limit. Delaying email send for campaign contact with ID: {}.", mailbox.getId(), campaignContactInfoDto.getId());
            delayNextSendAt(campaignContactInfoDto, 1, campaignDto);
            return;
        }

        // Check 5 — outside the campaign's sending window; snap nextSendAt to the next open slot
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime snappedSendAt = sendingWindowService.nextValidSendTime(now, campaignContactInfoDto, campaignDto);
        if (snappedSendAt.isAfter(now)) {
            log.info("Campaign contact with ID: {} is outside the sending window. Next send snapped to {}.", campaignContactInfoDto.getId(), snappedSendAt);
            campaignContactInfoDto.setNextSendAt(snappedSendAt);
            campaignContactService.saveCampaignContact(campaignContactInfoDto);
            return;
        }
        TenantDto tenantDto = tenantService.getTenant(tenantId);
        try {
            sendCampaignEmail(tenantDto, workspaceDto, campaignDto, campaignContactInfoDto, campaignEmail, mailbox);
        } catch (MailboxTokenExpiredException e) {
            log.error("Mailbox token expired for mailbox ID: {}. Marking as expired and delaying contact ID: {} by 1 day.", mailbox.getId(), campaignContactInfoDto.getId(), e);
            mailboxService.markTokenExpired(mailbox.getId());
            delayNextSendAt(campaignContactInfoDto, 1, campaignDto);
        } catch (Exception e) {
            log.error("Failed to send campaign email for contact ID: {} using mailbox ID: {}. Delaying retry by 1 day.", campaignContactInfoDto.getId(), mailbox.getId(), e);
            delayNextSendAt(campaignContactInfoDto, 1, campaignDto);
        }
    }

    private void sendCampaignEmail(
            TenantDto tenantDto,
            WorkspaceDto workspaceDto,
            CampaignDto campaignDto,
            CampaignContactInfoDto campaignContact,
            CampaignEmailDto campaignEmail,
            MailboxDto mailbox
    ) {

        UserDto userDto = userService.getUserById(mailbox.getUserId());
        String senderName = StringUtils.hasText(userDto.getName()) ? userDto.getName() : mailbox.getEmailAddress();
        String senderNumber = userDto.getPhoneNumber();

        ContactEmailDto contactEmailDto = mailMergeService.executeMailMerge(campaignContact, campaignEmail, senderName, senderNumber);
        List<RecipientDto> ccRecipients = getEmailsForCampaign(workspaceDto.getCcRecipients(), campaignDto.getCcRecipients(), tenantDto.getCcRecipients());
        List<RecipientDto> bccRecipients = getEmailsForCampaign(workspaceDto.getBccRecipients(), campaignDto.getBccRecipients(), tenantDto.getBccRecipients());

        // Add contact-level BD email as CC from tenant metadata
        tenantContactMetadataRepository.findByTenantIdAndLeadContactId(tenantDto.getId(), campaignContact.getContactId())
                .ifPresent(tcm -> {
                    if (StringUtils.hasText(tcm.getBdEmail())) {
                        RecipientDto bdRecipient = RecipientDto.builder()
                                .email(tcm.getBdEmail())
                                .name(tcm.getBdName())
                                .build();
                        List<RecipientDto> updatedCc = new ArrayList<>(ccRecipients != null ? ccRecipients : List.of());
                        boolean alreadyPresent = updatedCc.stream()
                                .anyMatch(r -> tcm.getBdEmail().equalsIgnoreCase(r.getEmail()));
                        if (!alreadyPresent) {
                            updatedCc.add(bdRecipient);
                            contactEmailDto.setCcRecipients(updatedCc);
                        }
                    }
                });

        if (contactEmailDto.getCcRecipients() == null) {
            contactEmailDto.setCcRecipients(ccRecipients);
        }
        contactEmailDto.setBccRecipients(bccRecipients);

        EmailDataDto emailDataDto = EmailDataDto.builder()
                .stepNumber(campaignEmail.getStepNumber())
                .emailPlatform(mailbox.getType())
                .emailDeliveryStatus(EmailDeliveryStatus.SENT)
                .build();
        String unsubscribeToken = contactOutreachStatusService.getContactOutreachStatus(tenantDto.getId(), campaignContact.getContactId())
                .getUnsubscribeToken();
        String unsubscribeUrl =
                cloudfrontUrl + "/unsubscribe?token=" + unsubscribeToken;

        switch (mailbox.getType()) {
            case OUTLOOK -> {
                AzureGraphMessageResponse azureResponse = azureEmailService.sendEmail(
                        contactEmailDto,
                        mailbox,
                        unsubscribeUrl);
                emailDataDto.setMessageId(azureResponse.getId());
                emailDataDto.setConversationId(azureResponse.getConversationId());
            }
            case GMAIL -> {
                GmailMessageResponse gmailResponse = gmailEmailService.sendEmail(
                        contactEmailDto,
                        mailbox,
                        unsubscribeUrl);
                emailDataDto.setMessageId(gmailResponse.getId());
                emailDataDto.setConversationId(gmailResponse.getThreadId());
            }
            case SMTP -> springMailService.sendEmail(
                    contactEmailDto,
                    mailbox,
                    unsubscribeUrl);
            case SES -> awsSESService.sendInteractionEmail(
                    contactEmailDto,
                    userDto,
                    mailbox.getEmailAddress(),
                    unsubscribeUrl);
            default -> {
                return;
            }
        }

        emailDataDto.setSentAt(LocalDateTime.now(ZoneOffset.UTC));

        List<EmailDataDto> emailDataDtoList = CollectionUtils.isEmpty(campaignContact.getEmailData()) ?
                new ArrayList<>() :
                new ArrayList<>(campaignContact.getEmailData());
        emailDataDtoList.add(emailDataDto);
        campaignContact.setEmailData(emailDataDtoList);
        campaignContact.setLastSentAt(LocalDateTime.now(ZoneOffset.UTC));

        Optional<CampaignEmailDto> nextCampaignEmail = campaignEmailService.getOptionalCampaignEmailByCampaignIdAndStepNumber(
                campaignContact.getCampaignId(),
                campaignContact.getCurrentStep() + 1
        );
        if (nextCampaignEmail.isPresent()) {
            campaignContact.setCurrentStep(campaignContact.getCurrentStep() + 1);
            LocalDateTime nextSendAt = sendingWindowService.nextValidSendTime(
                    LocalDateTime.now(ZoneOffset.UTC).plusDays(nextCampaignEmail.get().getDelayDays()),
                    campaignContact,
                    campaignDto
            );
            campaignContact.setNextSendAt(nextSendAt);
        } else {
            campaignContact.setStatus(CampaignContactStatus.COMPLETED);
            campaignContact.setNextSendAt(null);
        }
        campaignContactService.saveCampaignContact(campaignContact);

        CampaignEmailSentEvent emailSentEvent = new CampaignEmailSentEvent(
                this, tenantDto.getId(), workspaceDto.getId(), mailbox, campaignDto, campaignEmail, campaignContact);
        eventPublisher.publishEvent(emailSentEvent);

        if (!campaignContactService.isCurrentStepOngoing(campaignContact.getCampaignId(), campaignEmail.getStepNumber())) {
            campaignEmail.setStatus(CampaignEmailStatus.COMPLETED);
            campaignEmailService.saveCampaignEmail(campaignEmail);
            if (nextCampaignEmail.isEmpty()) {
                campaignDto.setStatus(CampaignStatus.COMPLETED);
                campaignService.markCampaignAsCompleted(campaignDto.getId());
                log.info("Campaign with ID: {} has been completed.", campaignDto.getId());
            }
        } else if (!Objects.equals(campaignEmail.getStatus(), CampaignEmailStatus.RUNNING)) {
            campaignEmail.setStatus(CampaignEmailStatus.RUNNING);
            campaignEmailService.saveCampaignEmail(campaignEmail);
        }
    }

    private void delayNextSendAt(CampaignContactInfoDto campaignContact, int daysDelay, CampaignDto campaignDto) {
        int effectiveDelay = (daysDelay <= 0) ? 1 : daysDelay;
        LocalDateTime delayedTime = getStartOfDelayedDate(effectiveDelay);
        campaignContact.setNextSendAt(sendingWindowService.nextValidSendTime(delayedTime, campaignContact, campaignDto));
        campaignContactService.saveCampaignContact(campaignContact);
    }

    private LocalDateTime getStartOfDelayedDate(int delayDays) {
        return LocalDate.now(ZoneOffset.UTC)
                .plusDays(delayDays)
                .atStartOfDay();
    }

    public static List<RecipientDto> getEmailsForCampaign(List<RecipientDto> workspaceRecipients,
                                                          List<RecipientDto> campaignRecipients,
                                                          List<RecipientDto> tenantRecipients) {
        List<RecipientDto> recipientDtoList = CollectionUtils.isEmpty(campaignRecipients) ?
                workspaceRecipients : campaignRecipients;

        if (!CollectionUtils.isEmpty(tenantRecipients)) {
            recipientDtoList = RecipientUtils.mergeRecipients(recipientDtoList, tenantRecipients);
        }
        return recipientDtoList;
    }
}
