package ai.leadplus.application.mailboxes;

import ai.leadplus.domain.mailboxes.Mailbox;
import ai.leadplus.domain.mailboxes.MailBoxType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailboxDto {

    private Long id;
    private Long userId;
    private Long workspaceId;
    private String emailAddress;
    private MailBoxType type;
    private MailboxProviderConfigDto metaData;
    private int emailsSentToday;
    private boolean active;
    private boolean tokenExpired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MailboxDto fromEntity(Mailbox mailBox) {
        if (mailBox == null) {
            return null;
        }
        return MailboxDto.builder()
                .id(mailBox.getId())
                .userId(mailBox.getUserId())
                .workspaceId(mailBox.getWorkspaceId())
                .emailAddress(mailBox.getEmailAddress())
                .type(mailBox.getType())
                .metaData(MailboxProviderConfigDto.fromEntity(mailBox.getMetaData()))
                .emailsSentToday(mailBox.getEmailsSentToday())
                .active(mailBox.isActive())
                .tokenExpired(mailBox.isTokenExpired())
                .createdAt(mailBox.getCreatedAt())
                .updatedAt(mailBox.getUpdatedAt())
                .build();
    }

    public Mailbox toEntity() {
        return Mailbox.builder()
                .id(id)
                .userId(userId)
                .workspaceId(workspaceId)
                .emailAddress(emailAddress)
                .type(type)
                .metaData(metaData.toEntity())
                .emailsSentToday(emailsSentToday)
                .active(active)
                .tokenExpired(tokenExpired)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
