package ai.leadplus.application.contactemails;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
public class ContactEmailCompletion {

    @JsonProperty(required = true)
    @JsonPropertyDescription("Subject line of the email")
    private String subject;

    @JsonProperty(required = true)
    @JsonPropertyDescription("Body of the email")
    private String body;
}

