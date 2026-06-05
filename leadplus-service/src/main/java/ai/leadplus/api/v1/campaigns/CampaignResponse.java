package ai.leadplus.api.v1.campaigns;

import ai.leadplus.application.campaigns.CampaignInfoDto;
import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.leads.LeadFilterCriteria;
import ai.leadplus.domain.campaigns.CampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponse {

    private Long id;
    private String name;
    private Long tenantId;
    private Long workspaceId;
    private String industry;
    private Long templateId;
    private String sendingMailboxId;
    private String approvedBy;
    private LocalDate scheduledStart;
    private CampaignStatus status;
    private List<RecipientDto> ccRecipients;
    private List<RecipientDto> bccRecipients;
    private long totalCompanies;
    private long totalContacts;
    private TargetingCriteriaResponse targetingCriteria;
    private LeadFilterCriteria leadFilter;
    private List<CampaignCompanyResponse> campaignCompanies;
    private LocalDateTime launchedAt;

    public static CampaignResponse fromDto(CampaignInfoDto campaignInfoDto) {
        if (campaignInfoDto == null) {
            return null;
        }
        return CampaignResponse.builder()
                .id(campaignInfoDto.getId())
                .name(campaignInfoDto.getName())
                .tenantId(campaignInfoDto.getTenantId())
                .workspaceId(campaignInfoDto.getWorkspaceId())
                .templateId(campaignInfoDto.getTemplateId())
                .sendingMailboxId(campaignInfoDto.getSendingMailboxId() != null
                        ? String.valueOf(campaignInfoDto.getSendingMailboxId()) : null)
                .industry(campaignInfoDto.getIndustry())
                .approvedBy(campaignInfoDto.getApprovedBy())
                .scheduledStart(campaignInfoDto.getScheduledStart())
                .status(campaignInfoDto.getStatus())
                .ccRecipients(campaignInfoDto.getCcRecipients())
                .bccRecipients(campaignInfoDto.getBccRecipients())
                .totalCompanies(campaignInfoDto.getTotalCompanies())
                .totalContacts(campaignInfoDto.getTotalContacts())
                .targetingCriteria(TargetingCriteriaResponse.fromDto(campaignInfoDto.getTargetingCriteria()))
                .leadFilter(campaignInfoDto.getLeadFilter())
                .campaignCompanies(CollectionUtils.isEmpty(campaignInfoDto.getCampaignCompanies()) ?
                        null :
                        campaignInfoDto.getCampaignCompanies().stream()
                                .map(CampaignCompanyResponse::fromDto)
                                .toList())
                .launchedAt(campaignInfoDto.getLaunchedAt())
                .build();
    }
}
