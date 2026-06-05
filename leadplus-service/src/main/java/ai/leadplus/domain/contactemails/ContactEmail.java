package ai.leadplus.domain.contactemails;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import ai.leadplus.domain.common.RecipientListConverter;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import ai.leadplus.domain.common.Recipient;
import ai.leadplus.domain.mailboxes.MailBoxType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
public class ContactEmail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long tenantId;
    private Long workspaceId;
    private Long campaignId;
    private Long contactId;
    private String messageId;
    private String conversationId;
    @Convert(converter = RecipientListConverter.class)
    private List<Recipient> toRecipients;
    @Convert(converter = RecipientListConverter.class)
    private List<Recipient> ccRecipients;
    @Convert(converter = RecipientListConverter.class)
    private List<Recipient> bccRecipients;
    private String subject;
    private String body;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "attachment_ids", columnDefinition = "varchar(255)[]")
    private List<String> attachmentIds;
    @Enumerated(EnumType.STRING)
    private MailBoxType platform;
    @Enumerated(EnumType.STRING)
    private ContactEmailType type;
    @CreatedBy
    private Long createdBy;
    @CreatedDate
    private LocalDateTime createdAt;
}
