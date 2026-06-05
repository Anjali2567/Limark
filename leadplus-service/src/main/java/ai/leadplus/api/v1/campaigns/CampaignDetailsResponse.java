package ai.leadplus.api.v1.campaigns;

import ai.leadplus.application.campaigns.CampaignDto;
import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.leads.LeadFilterCriteria;
import ai.leadplus.domain.campaigns.CampaignStatus;
import ai.leadplus.domain.campaigns.SendingWindow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignDetailsResponse {
    private Long id;
    private String name;
    private Long tenantId;
    private Long workspaceId;
    private Long templateId;
    private Long sendingMailboxId;
    private String approvedBy;
    private LocalDate scheduledStart;
    private CampaignStatus status;
    private List<RecipientDto> ccRecipients;
    private List<RecipientDto> bccRecipients;
    private TargetingCriteriaResponse targetingCriteria;
    private LeadFilterCriteria leadFilter;
    private SendingWindow sendingWindow;

    public static CampaignDetailsResponse fromDto(CampaignDto campaignDto) {
        if (campaignDto == null) {
            return null;
        }
        return CampaignDetailsResponse.builder()
                .id(campaignDto.getId())
                .name(campaignDto.getName())
                .tenantId(campaignDto.getTenantId())
                .workspaceId(campaignDto.getWorkspaceId())
                .templateId(campaignDto.getTemplateId())
                .sendingMailboxId(campaignDto.getSendingMailboxId())
                .approvedBy(campaignDto.getApprovedBy())
                .scheduledStart(campaignDto.getScheduledStart())
                .status(campaignDto.getStatus())
                .ccRecipients(campaignDto.getCcRecipients())
                .bccRecipients(campaignDto.getBccRecipients())
                .targetingCriteria(TargetingCriteriaResponse.fromDto(campaignDto.getTargetingCriteria()))
                .leadFilter(campaignDto.getLeadFilter())
                .sendingWindow(campaignDto.getSendingWindow())
                .build();
    }
}
