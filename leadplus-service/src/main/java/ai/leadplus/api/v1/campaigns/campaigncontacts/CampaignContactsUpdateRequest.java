package ai.leadplus.api.v1.campaigns.campaigncontacts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignContactsUpdateRequest {
    private List<Long> selectedCampaignContactIds;
    private List<Long> excludedCampaignContactIds;
}
