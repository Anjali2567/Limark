package ai.leadplus.domain.tenants;

import ai.leadplus.domain.common.MailboxProviderConfigConverter;
import ai.leadplus.domain.common.ModuleListConverter;
import ai.leadplus.domain.common.RecipientListConverter;
import ai.leadplus.domain.common.Recipient;
import ai.leadplus.domain.mailboxes.MailBoxType;
import ai.leadplus.domain.mailboxes.MailboxProviderConfig;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String domain;
    @Column(columnDefinition = "text")
    private String profileContext;
    private Long ownerId;
    @Convert(converter = ModuleListConverter.class)
    @Column(name = "modules", columnDefinition = "varchar(255)[]")
    private List<Module> modules;
    private String zohoUserId;
    private String zohoEmail;
    private String zohoRefreshToken;
    private LocalDateTime zohoConnectedAt;
    private String hubspotRefreshToken;
    private String hubspotUserId;
    private String hubspotEmail;
    private LocalDateTime hubspotConnectedAt;
    private String announcementFromEmail;
    private String announcementSenderName;
    @Enumerated(EnumType.STRING)
    private MailBoxType announcementType;
    @Convert(converter = MailboxProviderConfigConverter.class)
    private MailboxProviderConfig announcementMetaData;
    @Builder.Default
    @Convert(converter = RecipientListConverter.class)
    private List<Recipient> ccRecipients = new ArrayList<>();
    @Builder.Default
    @Convert(converter = RecipientListConverter.class)
    private List<Recipient> bccRecipients = new ArrayList<>();
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
