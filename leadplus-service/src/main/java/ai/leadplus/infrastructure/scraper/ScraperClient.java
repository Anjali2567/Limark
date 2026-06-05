package ai.leadplus.infrastructure.scraper;

import ai.leadplus.infrastructure.scraper.dto.CreateScraperJobRequest;
import ai.leadplus.infrastructure.scraper.dto.ScraperJobListResponse;
import ai.leadplus.infrastructure.scraper.dto.ScraperJobResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScraperClient {

    private final ScraperProperties properties;
    private final RestTemplate restTemplate;

    public ScraperJobResponse createJob(CreateScraperJobRequest request) {
        String url = properties.getBaseUrl() + "/jobs";
        HttpEntity<CreateScraperJobRequest> entity = new HttpEntity<>(request, apiKeyHeaders());
        return restTemplate.postForObject(url, entity, ScraperJobResponse.class);
    }

    public ScraperJobListResponse getJobsAfter(LocalDateTime completedAfter) {
        String url = UriComponentsBuilder.fromUriString(properties.getBaseUrl() + "/jobs")
                .queryParam("completed_after", completedAfter)
                .queryParam("limit", 100)
                .toUriString();
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(apiKeyHeaders()), ScraperJobListResponse.class).getBody();
    }

    private HttpHeaders apiKeyHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", properties.getApiKey());
        return headers;
    }
}
