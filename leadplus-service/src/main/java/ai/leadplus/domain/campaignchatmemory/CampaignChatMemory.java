package ai.leadplus.domain.campaignchatmemory;

import jakarta.persistence.Entity;
import jakarta.persistence.Convert;
import ai.leadplus.domain.common.LeadFilterConverter;
import ai.leadplus.domain.common.TargetingCriteriaConverter;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import ai.leadplus.domain.common.LeadFilter;
import ai.leadplus.domain.common.TargetingCriteria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
public class CampaignChatMemory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long campaignId;
    private String name;
    private Long tenantId;
    private Long workspaceId;
    private String industry;
    @Convert(converter = TargetingCriteriaConverter.class)
    private TargetingCriteria targetingCriteria;
    @Convert(converter = LeadFilterConverter.class)
    private LeadFilter leadFilter;
    private Integer contactLimit;
    @CreatedBy
    private Long createdBy;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedBy
    private Long updatedBy;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    private LocalDateTime lastSearchAt;
}