package ai.leadplus.infrastructure.zoho.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ZohoUserResponse {

    @JsonProperty("First_Name")
    private String firstName;

    @JsonProperty("Last_Name")
    private String lastName;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("ZUID")
    private String zuid;

    @JsonProperty("error")
    private String error;
}
