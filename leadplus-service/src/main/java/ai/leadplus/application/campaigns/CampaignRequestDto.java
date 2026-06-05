package ai.leadplus.application.campaigns;

import ai.leadplus.domain.campaigns.SendingWindow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignRequestDto {
    private String name;
    private SendingWindow sendingWindow;
}
