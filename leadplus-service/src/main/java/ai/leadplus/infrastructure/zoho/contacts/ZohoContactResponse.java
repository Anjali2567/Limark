package ai.leadplus.infrastructure.zoho.contacts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ZohoContactResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("First_Name")
    private String firstName;

    @JsonProperty("Last_Name")
    private String lastName;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Phone")
    private String phone;

    @JsonProperty("Mobile")
    private String mobile;

    @JsonProperty("Other_City")
    private String otherCity;

    @JsonProperty("Other_State")
    private String otherState;

    @JsonProperty("Other_Country")
    private String otherCountry;

    @JsonProperty("Other_Zip")
    private String otherZip;

    @JsonProperty("Account_Name")
    private ZohoAccountResponse accountName;

    @Data
    public static class ZohoAccountResponse {

        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;
    }
}