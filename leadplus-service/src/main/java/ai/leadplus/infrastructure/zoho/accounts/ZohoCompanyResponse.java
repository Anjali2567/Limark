package ai.leadplus.infrastructure.zoho.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ZohoCompanyResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("Account_Name")
    private String name;
}
