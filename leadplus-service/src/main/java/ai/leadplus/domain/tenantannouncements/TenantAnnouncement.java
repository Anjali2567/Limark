package ai.leadplus.domain.tenantannouncements;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import ai.leadplus.domain.common.RecipientListConverter;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import ai.leadplus.domain.common.Recipient;
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
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
public class TenantAnnouncement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long tenantId;
    private String name;
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
    private TenantAnnouncementStatus status;
    private boolean active;
    private LocalDateTime launchedAt;
    @CreatedBy
    private Long createdBy;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedBy
    private Long updatedBy;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
