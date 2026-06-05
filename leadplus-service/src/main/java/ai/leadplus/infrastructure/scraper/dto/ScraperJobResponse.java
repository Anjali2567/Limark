package ai.leadplus.infrastructure.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScraperJobResponse {
    private String id;
    private String url;
    private String status;
    @JsonProperty("external_id")
    private String externalId;
    private Map<String, Object> result;
    private Map<String, Object> error;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("started_at")
    private String startedAt;
    @JsonProperty("completed_at")
    private String completedAt;
    @JsonProperty("retry_count")
    private int retryCount;
    private Map<String, Object> metadata;
}
