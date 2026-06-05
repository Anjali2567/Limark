package ai.leadplus.infrastructure.hubspot.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HubSpotTokenInfoResponse {

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("user")
    private String email;

    @JsonProperty("hub_id")
    private Long hubId;

    @JsonProperty("hub_domain")
    private String hubDomain;

    @JsonProperty("error")
    private String error;
}