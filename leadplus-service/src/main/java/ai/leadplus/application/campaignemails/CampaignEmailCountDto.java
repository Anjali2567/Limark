package ai.leadplus.application.campaignemails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampaignEmailCountDto {
    private Long campaignId;
    private int count;
}
