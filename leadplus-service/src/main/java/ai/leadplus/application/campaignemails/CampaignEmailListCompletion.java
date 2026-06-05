package ai.leadplus.application.campaignemails;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

import java.util.List;

@Data
public class CampaignEmailListCompletion {

    @JsonProperty(required = true)
    @JsonPropertyDescription("List of campaign emails")
    private List<CampaignEmailCompletion> campaignEmails;
}
