package ai.leadplus.application.campaignemails;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
public class CampaignEmailCompletion {

    @JsonProperty(required = true)
    @JsonPropertyDescription("Step number of the email in the campaign sequence")
    private int stepNumber;

    @JsonProperty(required = true)
    @JsonPropertyDescription("Subject line of the email")
    private String subject;

    @JsonProperty(required = true)
    @JsonPropertyDescription("Body of the email")
    private String body;
}
