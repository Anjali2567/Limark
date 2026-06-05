package ai.leadplus.api.v1.campaigns.campaignemails;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignEmailGeneratorRequest {
    @NotBlank
    private String userPrompt;
}
