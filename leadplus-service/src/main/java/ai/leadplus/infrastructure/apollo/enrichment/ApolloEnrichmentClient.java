package ai.leadplus.infrastructure.apollo.enrichment;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.infrastructure.apollo.ApolloProperties;
import ai.leadplus.infrastructure.apollo.ApolloUtilities;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "apollo.enabled", havingValue = "true")
public class ApolloEnrichmentClient {

    private final ApolloProperties apolloProperties;
    private final ApolloUtilities apolloUtilities;
    private final RestTemplate restTemplate;

    public String enrichByApolloId(String apolloId) {
        if (!StringUtils.hasText(apolloId)) {
            throw new BadRequestException("Apollo ID cannot be empty");
        }
        log.info("Apollo PeopleEnrichment with the Apollo ID {}", apolloId);
        HttpHeaders headers = apolloUtilities.createHeader();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = apolloProperties.getBaseUrl() + "/people/match?id=" + apolloId;
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        log.info("Successfully fetched people data from Apollo");
        return response.getBody();
    }

    public String bulkEnrichByApolloId(List<String> apolloIds) {
        if (CollectionUtils.isEmpty(apolloIds)) {
            throw new BadRequestException("Apollo IDs cannot be empty");
        }
        log.info("Apollo CompanyEnrichment with the Apollo IDs {}", apolloIds);
        HttpHeaders headers = apolloUtilities.createHeader();
        List<ApolloEnrichmentBulkRequest.Id> ids = apolloIds.stream()
                .map(ApolloEnrichmentBulkRequest.Id::new)
                .toList();
        ApolloEnrichmentBulkRequest request = new ApolloEnrichmentBulkRequest(ids);
        HttpEntity<ApolloEnrichmentBulkRequest> entity = new HttpEntity<>(request, headers);
        String url = apolloProperties.getBaseUrl() + "/people/bulk_match";
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        log.info("Successfully fetched company data from Apollo");
        return response.getBody();
    }
}
