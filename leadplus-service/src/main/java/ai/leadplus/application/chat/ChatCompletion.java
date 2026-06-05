package ai.leadplus.application.chat;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
public class ChatCompletion {
    private String response;
    @JsonPropertyDescription("Reasoning behind the response")
    private String reasoning;
}
