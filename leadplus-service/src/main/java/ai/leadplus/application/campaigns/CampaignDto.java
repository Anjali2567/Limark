package ai.leadplus.application.campaigns;

import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.common.TargetingCriteriaDto;
import ai.leadplus.application.common.utils.RecipientUtils;
import ai.leadplus.application.leads.LeadFilterCriteria;
import ai.leadplus.domain.campaigns.Campaign;
import ai.leadplus.domain.campaigns.CampaignStatus;
import ai.leadplus.domain.campaigns.SendingWindow;
import ai.leadplus.domain.common.TargetingCriteria;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignDto {
    private Long id;
    private String name;
    private Long tenantId;
    private Long workspaceId;
    private String industry;
    private Long templateId;
    private Long sendingMailboxId;
    private String approvedBy;
    private TargetingCriteriaDto targetingCriteria;
    private LeadFilterCriteria leadFilter;
    private LocalDate scheduledStart;
    private CampaignStatus status;
    private List<RecipientDto> ccRecipients;
    private List<RecipientDto> bccRecipients;
    private SendingWindow sendingWindow;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime launchedAt;

    public static CampaignDto fromEntity(Campaign campaign) {
        return baseBuilder(CampaignDto.builder(), campaign)
                .build();
    }

    @SuppressWarnings("unchecked")
    protected static <T extends CampaignDtoBuilder<?, ?>> T baseBuilder(T builder, Campaign campaign) {
        return (T) builder
                .id(campaign.getId())
                .name(campaign.getName())
                .tenantId(campaign.getTenantId())
                .workspaceId(campaign.getWorkspaceId())
                .industry(campaign.getIndustry())
                .templateId(campaign.getTemplateId())
                .sendingMailboxId(campaign.getSendingMailboxId())
                .approvedBy(campaign.getApprovedBy())
                .targetingCriteria(campaign.getTargetingCriteria() != null ?
                        TargetingCriteriaDto.fromEntity(campaign.getTargetingCriteria()) :
                        new TargetingCriteriaDto())
                .leadFilter(campaign.getLeadFilter())
                .scheduledStart(campaign.getScheduledStart())
                .status(campaign.getStatus())
                .ccRecipients(RecipientUtils.mapToDto(campaign.getCcRecipients()))
                .bccRecipients(RecipientUtils.mapToDto(campaign.getBccRecipients()))
                .sendingWindow(campaign.getSendingWindow())
                .createdBy(campaign.getCreatedBy())
                .createdAt(campaign.getCreatedAt())
                .updatedBy(campaign.getUpdatedBy())
                .updatedAt(campaign.getUpdatedAt())
                .launchedAt(campaign.getLaunchedAt());
    }

    public Campaign toEntity() {
        return Campaign.builder()
                .id(id)
                .name(name)
                .tenantId(tenantId)
                .workspaceId(workspaceId)
                .industry(industry)
                .templateId(templateId)
                .sendingMailboxId(sendingMailboxId)
                .approvedBy(approvedBy)
                .targetingCriteria(targetingCriteria != null ?
                        targetingCriteria.toEntity() :
                        new TargetingCriteria())
                .leadFilter(leadFilter)
                .scheduledStart(scheduledStart)
                .status(status)
                .ccRecipients(RecipientUtils.mapToEntity(ccRecipients))
                .bccRecipients(RecipientUtils.mapToEntity(bccRecipients))
                .sendingWindow(sendingWindow)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .launchedAt(launchedAt)
                .build();
    }

    public String toCleanedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Campaign {");
        if (StringUtils.hasText(name)) {
            sb.append("Name: ").append(name);
        }
        if (StringUtils.hasText(industry)) {
            sb.append("\nIndustry: ").append(industry);
        }
        if (targetingCriteria != null) {
            sb.append("\n").append(targetingCriteria.toCleanedString());
        }
        sb.append("}");
        return sb.toString().trim();
    }
}
