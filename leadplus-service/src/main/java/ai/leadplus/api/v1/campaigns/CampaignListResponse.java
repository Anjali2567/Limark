package ai.leadplus.api.v1.campaigns;

import ai.leadplus.application.campaigns.CampaignListDto;
import ai.leadplus.domain.campaigns.CampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignListResponse {
    private Long id;
    private String name;
    private Long tenantId;
    private Long workspaceId;
    private String industry;
    private Long templateId;
    private LocalDate scheduledStart;
    private CampaignStatus status;
    private Integer totalCompanies;
    private Integer totalContacts;
    private Long sentEmails;
    private Long openedEmails;
    private Double progress;

    public static CampaignListResponse fromDto(CampaignListDto campaignListDto) {
        return CampaignListResponse.builder()
                .id(campaignListDto.getId())
                .name(campaignListDto.getName())
                .tenantId(campaignListDto.getTenantId())
                .workspaceId(campaignListDto.getWorkspaceId())
                .industry(campaignListDto.getIndustry())
                .templateId(campaignListDto.getTemplateId())
                .scheduledStart(campaignListDto.getScheduledStart())
                .status(campaignListDto.getStatus())
                .totalContacts(campaignListDto.getTotalContacts())
                .totalCompanies(campaignListDto.getTotalCompanies())
                .sentEmails(campaignListDto.getSentEmails())
                .openedEmails(campaignListDto.getOpenedEmails())
                .progress(campaignListDto.getProgress())
                .build();
    }
}
