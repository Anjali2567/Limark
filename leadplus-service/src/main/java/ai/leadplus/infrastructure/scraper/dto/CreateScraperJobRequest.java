package ai.leadplus.infrastructure.scraper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class CreateScraperJobRequest {
    private String url;
    @JsonProperty("external_id")
    private String externalId;
    private ScraperJobOptions options;
    private List<Map<String, Object>> actions;
}
