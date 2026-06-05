package ai.leadplus.api.v1.campaigns.campaigncontacts;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCampaignContactsRequest {
    @NotEmpty(message = "Contact IDs list cannot be empty")
    private List<Long> contactIds;
}
