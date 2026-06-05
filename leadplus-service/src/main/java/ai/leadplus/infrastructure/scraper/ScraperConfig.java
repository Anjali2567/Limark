package ai.leadplus.infrastructure.scraper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class ScraperConfig {

    @Bean
    public ScraperCompanyJobTemplate scraperCompanyJobTemplate(ObjectMapper objectMapper) throws IOException {
        return objectMapper.readValue(
                new ClassPathResource("scraper/company-job-request.json").getInputStream(),
                ScraperCompanyJobTemplate.class
        );
    }

    @Bean
    public ScraperJobDetailTemplate scraperJobDetailTemplate(ObjectMapper objectMapper) throws IOException {
        return objectMapper.readValue(
                new ClassPathResource("scraper/job-detail-request.json").getInputStream(),
                ScraperJobDetailTemplate.class
        );
    }
}
