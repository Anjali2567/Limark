package ai.leadplus.api.v1.campaigns;

import ai.leadplus.application.campaigns.CampaignRequestDto;
import ai.leadplus.domain.campaigns.SendingWindow;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignRequest {
    @NotBlank
    private String name;

    private SendingWindow sendingWindow;

    public CampaignRequestDto toDto() {
        return CampaignRequestDto.builder()
                .name(name)
                .sendingWindow(sendingWindow)
                .build();
    }
}
