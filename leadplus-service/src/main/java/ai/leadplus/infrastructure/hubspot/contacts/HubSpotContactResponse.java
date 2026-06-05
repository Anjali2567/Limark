package ai.leadplus.infrastructure.hubspot.contacts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HubSpotContactResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("properties")
    private HubSpotContactProperties properties;

    @JsonProperty("associations")
    private HubSpotContactAssociations associations;
}