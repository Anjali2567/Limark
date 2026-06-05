package ai.leadplus.application.contactemails;

import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.common.utils.RecipientUtils;
import ai.leadplus.domain.contactemails.ContactEmail;
import ai.leadplus.domain.contactemails.ContactEmailType;
import ai.leadplus.domain.mailboxes.MailBoxType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactEmailDto {

    private Long id;
    private Long tenantId;
    private Long workspaceId;
    private Long campaignId;
    private Long contactId;
    private String messageId;
    private String conversationId;
    private List<RecipientDto> toRecipients;
    private List<RecipientDto> ccRecipients;
    private List<RecipientDto> bccRecipients;
    private String subject;
    private String body;
    private List<String> attachmentIds;
    private MailBoxType platform;
    private ContactEmailType type;
    private Long createdBy;
    private LocalDateTime createdAt;

    public static ContactEmailDto fromEntity(ContactEmail contactEmail) {
        return ContactEmailDto.builder()
                .id(contactEmail.getId())
                .tenantId(contactEmail.getTenantId())
                .workspaceId(contactEmail.getWorkspaceId())
                .campaignId(contactEmail.getCampaignId())
                .contactId(contactEmail.getContactId())
                .messageId(contactEmail.getMessageId())
                .conversationId(contactEmail.getConversationId())
                .toRecipients(RecipientUtils.mapToDto(contactEmail.getToRecipients()))
                .ccRecipients(RecipientUtils.mapToDto(contactEmail.getCcRecipients()))
                .bccRecipients(RecipientUtils.mapToDto(contactEmail.getBccRecipients()))
                .subject(contactEmail.getSubject())
                .body(contactEmail.getBody())
                .attachmentIds(contactEmail.getAttachmentIds())
                .platform(contactEmail.getPlatform())
                .type(contactEmail.getType())
                .createdBy(contactEmail.getCreatedBy())
                .createdAt(contactEmail.getCreatedAt())
                .build();
    }

    public ContactEmail toEntity() {
        return ContactEmail.builder()
                .id(id)
                .tenantId(tenantId)
                .workspaceId(workspaceId)
                .campaignId(campaignId)
                .contactId(contactId)
                .messageId(messageId)
                .conversationId(conversationId)
                .toRecipients(RecipientUtils.mapToEntity(toRecipients))
                .ccRecipients(RecipientUtils.mapToEntity(ccRecipients))
                .bccRecipients(RecipientUtils.mapToEntity(bccRecipients))
                .subject(subject)
                .body(body)
                .attachmentIds(attachmentIds)
                .platform(platform)
                .type(type)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .build();
    }
}
