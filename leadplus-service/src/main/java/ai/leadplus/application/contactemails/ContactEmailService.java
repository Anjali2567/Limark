package ai.leadplus.application.contactemails;

import ai.leadplus.application.azure.AzureEmailService;
import ai.leadplus.application.campaignorchestrator.MailMergeService;
import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.common.utils.RecipientUtils;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.google.GmailEmailService;
import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.application.leadcontact.LeadContactService;
import ai.leadplus.application.mailboxes.MailboxDto;
import ai.leadplus.application.mailboxes.MailboxService;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.application.tenants.TenantDto;
import ai.leadplus.application.tenants.TenantService;
import ai.leadplus.application.leads.TenantContactMetadataDto;
import ai.leadplus.domain.tenantcontactmetadata.TenantContactMetadataRepository;
import ai.leadplus.domain.contactemails.ContactEmail;
import ai.leadplus.domain.contactemails.ContactEmailRepository;
import ai.leadplus.domain.contactemails.ContactEmailType;
import ai.leadplus.infrastructure.azure.graph.emails.AzureGraphMessageResponse;
import ai.leadplus.infrastructure.google.gmail.GmailMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactEmailService {

    private final ContactEmailRepository contactEmailRepository;
    private final LeadContactService leadContactService;
    private final MailboxService mailboxService;
    private final TenantService tenantService;
    private final UserService userService;
    private final TenantContactMetadataRepository tenantContactMetadataRepository;
    private final AzureEmailService azureEmailService;
    private final GmailEmailService gmailEmailService;
    private final MailMergeService mailMergeService;
    private final ApplicationEventPublisher eventPublisher;

    public void sendEmail(Long mailboxId, Long userId, ContactEmailDto contactEmailDto) {
        if (contactEmailDto.getContactId() == null) {
            throw new BadRequestException("Contact ID is required");
        }
        TenantDto tenantDto = tenantService.getTenant(contactEmailDto.getTenantId());
        LeadContactDto leadContactDto = leadContactService.getContactById(contactEmailDto.getContactId());
        MailboxDto mailbox = mailboxService.getAccessibleMailboxById(mailboxId, contactEmailDto.getWorkspaceId(), userId);
        UserDto senderUser = userService.getUserById(mailbox.getUserId());
        TenantContactMetadataDto tenantContactMetadata = tenantContactMetadataRepository
                .findByTenantIdAndLeadContactId(contactEmailDto.getTenantId(), contactEmailDto.getContactId())
                .map(TenantContactMetadataDto::fromEntity)
                .orElse(null);

        List<RecipientDto> ccRecipients = RecipientUtils.mergeRecipients(tenantDto.getCcRecipients(), contactEmailDto.getCcRecipients());
        List<RecipientDto> bccRecipients = RecipientUtils.mergeRecipients(tenantDto.getBccRecipients(), contactEmailDto.getBccRecipients());
        RecipientDto recipientDto = RecipientDto
                .builder()
                .email(leadContactDto.getEmail())
                .name(leadContactDto.getFirstName())
                .build();

        contactEmailDto.setToRecipients(List.of(recipientDto));
        contactEmailDto = mailMergeService.executeMailMerge(leadContactDto, contactEmailDto, senderUser, tenantContactMetadata);

        contactEmailDto.setCcRecipients(ccRecipients);
        contactEmailDto.setBccRecipients(bccRecipients);

        switch (mailbox.getType()) {
            case OUTLOOK -> {
                AzureGraphMessageResponse azureResponse = azureEmailService.sendEmail(
                        contactEmailDto,
                        mailbox);
                contactEmailDto.setMessageId(azureResponse.getId());
                contactEmailDto.setConversationId(azureResponse.getConversationId());
            }
            case GMAIL -> {
                GmailMessageResponse gmailResponse = gmailEmailService.sendEmail(
                        contactEmailDto,
                        mailbox);
                contactEmailDto.setMessageId(gmailResponse.getId());
                contactEmailDto.setConversationId(gmailResponse.getThreadId());
            }
            default -> throw new BadRequestException("Unsupported mailbox type: " + mailbox.getType());
        }
        contactEmailDto.setPlatform(mailbox.getType());
        contactEmailDto.setType(ContactEmailType.DIRECT);
        ContactEmailDto savedEmail = saveEmail(contactEmailDto);

        ContactEmailSentEvent contactEmailSentEvent = new ContactEmailSentEvent(this, savedEmail);
        eventPublisher.publishEvent(contactEmailSentEvent);
    }

    public ContactEmailDto saveEmail(ContactEmailDto contactEmailDto) {
        ContactEmail contactEmail = contactEmailRepository.save(contactEmailDto.toEntity());
        return ContactEmailDto.fromEntity(contactEmail);
    }

    public long countByTenantId(Long tenantId) {
        return contactEmailRepository.countByTenantId(tenantId);
    }

    public LocalDateTime findLatestCreatedAtByTenantId(Long tenantId) {
        return contactEmailRepository.findTopByTenantIdOrderByCreatedAtDesc(tenantId)
                .map(ContactEmail::getCreatedAt)
                .orElse(null);
    }
}
