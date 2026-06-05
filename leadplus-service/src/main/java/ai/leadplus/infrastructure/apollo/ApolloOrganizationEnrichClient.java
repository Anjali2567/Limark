package ai.leadplus.infrastructure.apollo;

import ai.leadplus.application.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "apollo.enabled", havingValue = "true")
public class ApolloOrganizationEnrichClient {

    private final ApolloProperties apolloProperties;
    private final RestTemplate restTemplate;
    private final ApolloUtilities apolloUtilities;

    public String enrichOrganizationByDomain(String domainName) {
        log.info("ApolloOrganizationEnrich with the domain Name {}", domainName);
        if (!StringUtils.hasText(domainName)) {
            log.warn("Domain name is empty or null");
            return null;
        }

        HttpHeaders headers = apolloUtilities.createHeader();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = buildEnrichUrl(domainName);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            log.info("Successfully fetched organization data from Apollo");

            if (response.getBody() == null || response.getBody().isEmpty()) {
                log.warn("Received empty response body from Apollo for domain: {}", domainName);
                return null;
            }

            return response.getBody();
        } catch (Exception e) {
            log.error("Error while fetching organization data from Apollo: {}", e.getMessage());
            throw new ServiceUnavailableException("Apollo service is unavailable");
        }
    }

    private String buildEnrichUrl(String domainName) {
        String finalUrl = apolloProperties.getBaseUrl()
                + "/organizations/enrich?domain="
                + domainName;
        log.info("Built Apollo Organization Enrich URL: {}", finalUrl);
        return finalUrl;
    }
}
