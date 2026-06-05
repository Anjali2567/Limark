package ai.leadplus.infrastructure.hubspot.contacts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class HubSpotContactAssociations {

    @JsonProperty("companies")
    private Companies companies;

    @Data
    public static class Companies {

        @JsonProperty("results")
        private List<Result> results;
    }

    @Data
    public static class Result {

        @JsonProperty("id")
        private String id;

        @JsonProperty("type")
        private String type;
    }


}