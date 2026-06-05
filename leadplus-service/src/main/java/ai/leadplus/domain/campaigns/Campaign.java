package ai.leadplus.domain.campaigns;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Convert;
import ai.leadplus.domain.common.RecipientListConverter;
import ai.leadplus.domain.common.LeadFilterCriteriaConverter;
import ai.leadplus.domain.common.SendingWindowConverter;
import ai.leadplus.domain.common.TargetingCriteriaConverter;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import ai.leadplus.domain.common.Recipient;
import ai.leadplus.application.leads.LeadFilterCriteria;
import ai.leadplus.domain.common.TargetingCriteria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long tenantId;
    private Long workspaceId;
    private String industry;
    private Long templateId;
    private Long sendingMailboxId;
    private String approvedBy;
    @Convert(converter = TargetingCriteriaConverter.class)
    private TargetingCriteria targetingCriteria;
    @Convert(converter = LeadFilterCriteriaConverter.class)
    private LeadFilterCriteria leadFilter;
    private LocalDate scheduledStart;
    @Enumerated(EnumType.STRING)
    private CampaignStatus status;
    @Convert(converter = RecipientListConverter.class)
    private List<Recipient> ccRecipients;
    @Convert(converter = RecipientListConverter.class)
    private List<Recipient> bccRecipients;
    @Convert(converter = SendingWindowConverter.class)
    private SendingWindow sendingWindow;
    @CreatedBy
    private Long createdBy;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedBy
    private Long updatedBy;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    private LocalDateTime launchedAt;
}
