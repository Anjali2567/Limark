package ai.leadplus.application.campaigngenerator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
public class CampaignGeneratorCompletion {

    @JsonProperty(required = true)
    @JsonPropertyDescription("""
            - Concise, essential details only.
            """)
    private String response;
    @JsonProperty(required = true)
    @JsonPropertyDescription("Id of the campaign")
    private Long campaignId;
    @JsonPropertyDescription("""
            Suggest a name for the campaign.
            It should be short, descriptive, and in the context of campaign.
            Keep it under 40 characters.""")
    private String campaignName;
    @JsonPropertyDescription("""
            Suggest an industry for the campaign.
            It should be relevant to the campaign context.
            Keep it under 30 characters.
            """)
    private String campaignIndustry;
    @JsonPropertyDescription("If the AI was unable to generate a response due to insufficient context from the user")
    private boolean insufficientContext;
}
