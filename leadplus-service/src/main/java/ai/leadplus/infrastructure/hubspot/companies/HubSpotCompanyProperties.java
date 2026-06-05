package ai.leadplus.infrastructure.hubspot.companies;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HubSpotCompanyProperties {

    @JsonProperty("name")
    private String name;
}
