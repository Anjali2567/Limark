package ai.leadplus.application.leads;

import ai.leadplus.domain.campaigns.CampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentCampaignDto {
    private Long id;
    private String name;
    private CampaignStatus status;
}
