package ai.leadplus.application.campaignchatmemory;

import ai.leadplus.application.common.TargetingCriteriaDto;
import ai.leadplus.application.leads.LeadFilterCriteria;
import ai.leadplus.domain.campaignchatmemory.CampaignChatMemory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignChatMemoryDto {

    private Long id;
    private Long campaignId;
    private String name;
    private Long tenantId;
    private Long workspaceId;
    private String industry;
    private TargetingCriteriaDto targetingCriteria;
    private LeadFilterCriteria leadFilter;
    private Integer contactLimit;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime lastSearchAt;

    public static CampaignChatMemoryDto fromEntity(CampaignChatMemory campaignChatMemory) {
        return CampaignChatMemoryDto.builder()
                .id(campaignChatMemory.getId())
                .campaignId(campaignChatMemory.getCampaignId())
                .name(campaignChatMemory.getName())
                .tenantId(campaignChatMemory.getTenantId())
                .workspaceId(campaignChatMemory.getWorkspaceId())
                .industry(campaignChatMemory.getIndustry())
                .targetingCriteria(campaignChatMemory.getTargetingCriteria() != null ?
                        TargetingCriteriaDto.fromEntity(campaignChatMemory.getTargetingCriteria()) :
                        null)
                .leadFilter(campaignChatMemory.getLeadFilter() != null ?
                        LeadFilterCriteria.fromEntity(campaignChatMemory.getLeadFilter()) :
                        null)
                .contactLimit(campaignChatMemory.getContactLimit())
                .createdBy(campaignChatMemory.getCreatedBy())
                .createdAt(campaignChatMemory.getCreatedAt())
                .updatedBy(campaignChatMemory.getUpdatedBy())
                .updatedAt(campaignChatMemory.getUpdatedAt())
                .lastSearchAt(campaignChatMemory.getLastSearchAt())
                .build();
    }

    public CampaignChatMemory toEntity() {
        return CampaignChatMemory.builder()
                .id(id)
                .campaignId(campaignId)
                .name(name)
                .tenantId(tenantId)
                .workspaceId(workspaceId)
                .industry(industry)
                .targetingCriteria(targetingCriteria != null ?
                        targetingCriteria.toEntity() :
                        null)
                .leadFilter(leadFilter != null ?
                        leadFilter.toEntity() :
                        null)
                .contactLimit(contactLimit)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .lastSearchAt(lastSearchAt)
                .build();
    }
}