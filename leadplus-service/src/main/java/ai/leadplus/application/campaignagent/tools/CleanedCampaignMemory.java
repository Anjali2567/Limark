package ai.leadplus.application.campaignagent.tools;

import ai.leadplus.application.campaignchatmemory.CampaignChatMemoryDto;
import ai.leadplus.application.leads.LeadFilterCriteria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleanedCampaignMemory {

    private String name;
    private String industry;
    private LeadFilterCriteria leadFilter;

    public static CleanedCampaignMemory fromDto(CampaignChatMemoryDto dto) {
        return CleanedCampaignMemory.builder()
                .name(dto.getName())
                .industry(dto.getIndustry())
                .leadFilter(dto.getLeadFilter())
                .build();
    }
}
