package ai.leadplus.api.v1.contactemails;

import ai.leadplus.application.contactemails.ContactEmailDto;
import ai.leadplus.application.common.RecipientDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactEmailRequest {

    private String mailboxId;
    private List<RecipientDto> toRecipients;
    private List<RecipientDto> ccRecipients;
    private List<RecipientDto> bccRecipients;
    private String subject;
    private String body;
    private List<String> attachmentIds;

    public ContactEmailDto toDto(Long tenantId, Long workspaceId, Long contactId) {
        return ContactEmailDto.builder()
                .tenantId(tenantId)
                .workspaceId(workspaceId)
                .contactId(contactId)
                .toRecipients(toRecipients)
                .ccRecipients(ccRecipients)
                .bccRecipients(bccRecipients)
                .subject(subject)
                .body(body)
                .attachmentIds(attachmentIds)
                .build();
    }
}
