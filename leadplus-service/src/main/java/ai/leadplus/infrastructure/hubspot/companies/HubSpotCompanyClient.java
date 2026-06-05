package ai.leadplus.infrastructure.hubspot.companies;

import ai.leadplus.application.exception.ServiceUnavailableException;
import ai.leadplus.infrastructure.hubspot.HubSpotConfiguration;
import ai.leadplus.infrastructure.hubspot.auth.HubSpotAuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class HubSpotCompanyClient {

    private final HubSpotConfiguration hubSpotConfiguration;
    private final HubSpotAuthClient hubSpotAuthClient;
    private final RestTemplate restTemplate;

    private static final int PAGE_LIMIT = 100;

    public HubSpotCompaniesResponse fetchCompanies(String accessToken, String after) {
        String url = hubSpotConfiguration.getApiDomain() + "/crm/v3/objects/companies?limit=" + PAGE_LIMIT;
        if (after != null) url += "&after=" + after;

        HttpEntity<Void> request = new HttpEntity<>(hubSpotAuthClient.createHeader(accessToken));

        HubSpotCompaniesResponse response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                HubSpotCompaniesResponse.class
        ).getBody();

        if (response == null) {
            throw new ServiceUnavailableException("Failed to fetch HubSpot companies: empty response");
        }

        return response;
    }
}
