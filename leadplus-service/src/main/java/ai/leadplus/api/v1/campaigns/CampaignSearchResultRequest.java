package ai.leadplus.api.v1.campaigns;

import ai.leadplus.application.leads.LeadFilterCriteria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampaignSearchResultRequest {
    private List<Long> contactIds;
    private LeadFilterCriteria leadFilterCriteria;
}
