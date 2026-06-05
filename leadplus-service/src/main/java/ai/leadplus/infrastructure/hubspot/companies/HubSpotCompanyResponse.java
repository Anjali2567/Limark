package ai.leadplus.infrastructure.hubspot.companies;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HubSpotCompanyResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("properties")
    private HubSpotCompanyProperties properties;
}
