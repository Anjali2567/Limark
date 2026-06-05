package ai.leadplus.infrastructure.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScraperJobOptions {
    private String locale;
    private int timeout;
    private Viewport viewport;
    private String userAgent;
    private String timezoneId;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Viewport {
        private int width;
        private int height;
    }
}
