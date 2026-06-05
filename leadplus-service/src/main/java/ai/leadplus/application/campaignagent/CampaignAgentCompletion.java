package ai.leadplus.application.campaignagent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
public class CampaignAgentCompletion {

    @JsonProperty(required = true)
    @JsonPropertyDescription("""
            - Concise, essential details only.
            """)
    private String response;
    @JsonPropertyDescription("Id of the campaign")
    private Long campaignId;
    @JsonPropertyDescription("""
            Suggest a name for the campaign.
            It should be short, descriptive, and in the context of campaign.
            Keep it under 40 characters.""")
    private String campaignName;
    @JsonPropertyDescription("""
            Suggest an industry for the campaign from getIndustryList tool.
            It should be relevant to the campaign context.
            If not sure, return empty string.
            """)
    private String campaignIndustry;
}
