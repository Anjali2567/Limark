package ai.leadplus.infrastructure.scraper;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "scraper")
public class ScraperProperties {
    private String baseUrl;
    private String apiKey;
    private int jobsPerHour;
    private int lookBackDays;
    private int jobDetailJobsPerRun;
}
