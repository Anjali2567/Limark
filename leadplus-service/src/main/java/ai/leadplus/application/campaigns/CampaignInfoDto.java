package ai.leadplus.application.campaigns;

import ai.leadplus.application.campaignchatmemory.CampaignChatMemoryDto;
import ai.leadplus.application.common.TargetingCriteriaDto;
import ai.leadplus.application.leads.LeadFilterCriteria;
import ai.leadplus.domain.campaigns.Campaign;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignInfoDto extends CampaignDto {

    private long totalCompanies;
    private long totalContacts;
    private List<CampaignCompanyDto> campaignCompanies;
    private LeadFilterCriteria leadFilter;

    public static CampaignInfoDto fromEntity(Campaign campaign,
                                             long totalCompanies,
                                             long totalContacts,
                                             List<CampaignCompanyDto> campaignCompanies) {
        return baseBuilder(CampaignInfoDto.builder(), campaign)
                .totalCompanies(totalCompanies)
                .totalContacts(totalContacts)
                .campaignCompanies(campaignCompanies)
                .build();
    }

    public static CampaignInfoDto fromChatMemory(CampaignChatMemoryDto campaignChatMemoryDto,
                                                 TargetingCriteriaDto targetingCriteriaDto,
                                                 long totalCompanies,
                                                 long totalContacts,
                                                 List<CampaignCompanyDto> campaignCompanies) {
        return CampaignInfoDto.builder()
                .id(campaignChatMemoryDto.getId())
                .tenantId(campaignChatMemoryDto.getTenantId())
                .workspaceId(campaignChatMemoryDto.getWorkspaceId())
                .name(campaignChatMemoryDto.getName())
                .industry(campaignChatMemoryDto.getIndustry())
                .totalCompanies(totalCompanies)
                .totalContacts(totalContacts)
                .campaignCompanies(campaignCompanies)
                .targetingCriteria(targetingCriteriaDto)
                .leadFilter(campaignChatMemoryDto.getLeadFilter())
                .build();
    }
}