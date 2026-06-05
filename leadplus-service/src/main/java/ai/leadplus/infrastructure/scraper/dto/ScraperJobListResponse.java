package ai.leadplus.infrastructure.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScraperJobListResponse {
    private List<ScraperJobResponse> jobs;
    private int total;
    @JsonProperty("status_counts")
    private Map<String, Integer> statusCounts;
}
