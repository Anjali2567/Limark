package ai.leadplus.application.leadcompanyjob;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeadCompanyJobFilePayload {

    private String url;
    private DataWrapper data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataWrapper {
        @JsonDeserialize(using = CareersListDeserializer.class)
        private List<Careers> careers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Careers {
        @JsonProperty("$url")
        private String url;
        @JsonProperty("$title")
        private String title;
        private String pageType;
        private String companyName;
        private List<JobEntry> jobs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JobEntry {
        private String type;
        private String title;
        private String salary;
        private List<String> skills;
        private String jobUrl;
        private String applyUrl;
        private List<String> benefits;
        private String location;
        private String department;
        private String postedDate;
        private String description;
        private List<String> requirements;
    }

    public static class CareersListDeserializer extends StdDeserializer<List<Careers>> {

        public CareersListDeserializer() {
            super(List.class);
        }

        @Override
        public List<Careers> deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            List<Careers> result = new ArrayList<>();
            if (p.currentToken() == JsonToken.START_ARRAY) {
                while (p.nextToken() != JsonToken.END_ARRAY) {
                    result.add(p.readValueAs(Careers.class));
                }
            } else {
                result.add(p.readValueAs(Careers.class));
            }
            return result;
        }
    }
}
