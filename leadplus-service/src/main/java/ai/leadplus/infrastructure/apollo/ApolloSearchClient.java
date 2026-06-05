package ai.leadplus.infrastructure.apollo;

import ai.leadplus.application.apollospecification.ApolloSpecificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "apollo.enabled", havingValue = "true")
public class ApolloSearchClient {

    private final ApolloProperties apolloProperties;
    private final RestTemplate restTemplate;
    private final ApolloUtilities apolloUtilities;

    public String searchPeople(String domainName, ApolloSpecificationDto specificationDto) {
        log.info("ApolloPeopleSearch with the domain Name {}", domainName);
        HttpHeaders headers = apolloUtilities.createHeader();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = buildSearchUrl(domainName, specificationDto);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        log.info("Successfully fetched people data from Apollo");
        return response.getBody();
    }

    private String buildSearchUrl(String domainName, ApolloSpecificationDto specification) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(apolloProperties.getBaseUrl())
                .append("/mixed_people/api_search?");

        if (specification.isPersonTitleEnabled() && !CollectionUtils.isEmpty(specification.getPersonTitles())) {
            for (String title : specification.getPersonTitles()) {
                urlBuilder.append("person_titles[]=")
                        .append(URLEncoder.encode(title, StandardCharsets.UTF_8))
                        .append("&");
            }
        }

        if (specification.isPersonSeniorityEnabled() && !CollectionUtils.isEmpty(specification.getPersonSeniorities())) {
            for (String seniority : specification.getPersonSeniorities()) {
                urlBuilder.append("person_seniorities[]=")
                        .append(URLEncoder.encode(seniority, StandardCharsets.UTF_8))
                        .append("&");
            }
        }

        urlBuilder.append("q_organization_domains_list[]=")
                .append(URLEncoder.encode(domainName, StandardCharsets.UTF_8));

        String finalUrl = urlBuilder.toString();
        log.info("Built Apollo search URL: {}", finalUrl);
        return finalUrl;
    }
}
