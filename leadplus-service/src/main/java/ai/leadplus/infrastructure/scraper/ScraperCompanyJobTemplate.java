package ai.leadplus.infrastructure.scraper;

import ai.leadplus.infrastructure.scraper.dto.ScraperJobOptions;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ScraperCompanyJobTemplate {
    private ScraperJobOptions options;
    private List<Map<String, Object>> actions;
}
