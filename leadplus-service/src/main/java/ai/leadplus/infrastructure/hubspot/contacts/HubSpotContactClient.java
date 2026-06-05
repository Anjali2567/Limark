package ai.leadplus.infrastructure.hubspot.contacts;

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
public class HubSpotContactClient {

    private final HubSpotConfiguration hubSpotConfiguration;
    private final HubSpotAuthClient hubSpotAuthClient;
    private final RestTemplate restTemplate;

    private static final int PAGE_LIMIT = 100;

    public HubSpotContactsResponse fetchContacts(String accessToken, String after) {
        String url = hubSpotConfiguration.getApiDomain() + "/crm/v3/objects/contacts?limit=" + PAGE_LIMIT + "&associations=companies";
        if (after != null) url += "&after=" + after;

        HttpEntity<Void> request = new HttpEntity<>(hubSpotAuthClient.createHeader(accessToken));

        HubSpotContactsResponse response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                HubSpotContactsResponse.class
        ).getBody();

        if (response == null) {
            throw new ServiceUnavailableException("Failed to fetch HubSpot contacts: empty response");
        }

        return response;
    }
}