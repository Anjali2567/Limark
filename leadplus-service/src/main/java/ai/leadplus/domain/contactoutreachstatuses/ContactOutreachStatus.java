package ai.leadplus.domain.contactoutreachstatuses;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
public class ContactOutreachStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long contactId;
    private Long tenantId;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "current_campaign_ids", columnDefinition = "varchar(255)[]")
    private List<Long> currentCampaignIds;
    private LocalDateTime lastEmailAt;
    @Enumerated(EnumType.STRING)
    private GlobalOutreachStatus status;
    private LocalDateTime sequenceCompletedAt;
    private String unsubscribeToken;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
