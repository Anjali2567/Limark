package ai.leadplus.infrastructure.hubspot.user;

import ai.leadplus.application.exception.ServiceUnavailableException;
import ai.leadplus.infrastructure.hubspot.HubSpotConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubSpotUserClient {

    private final HubSpotConfiguration hubSpotConfiguration;
    private final RestTemplate restTemplate;

    public HubSpotUserResponse getUserInfo(String accessToken) {

        HubSpotTokenInfoResponse tokenInfo = fetchTokenInfo(accessToken);

        return HubSpotUserResponse.builder()
                .userId(tokenInfo.getUserId())
                .hubId(tokenInfo.getHubId())
                .hubDomain(tokenInfo.getHubDomain())
                .email(tokenInfo.getEmail())
                .build();
    }

    private HubSpotTokenInfoResponse fetchTokenInfo(String accessToken) {

        String url = hubSpotConfiguration.getApiDomain()
                + "/oauth/v1/access-tokens/" + accessToken;

        HttpEntity<Void> request = new HttpEntity<>(createHeader(accessToken));

        try {
            ResponseEntity<HubSpotTokenInfoResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    HubSpotTokenInfoResponse.class
            );

            if (response.getBody() == null || StringUtils.hasText(response.getBody().getError())) {
                throw new ServiceUnavailableException("Failed to fetch HubSpot token info");
            }

            return response.getBody();

        } catch (Exception e) {
            log.error("Error fetching HubSpot token info", e);
            throw new ServiceUnavailableException("Failed to fetch HubSpot token info: " + e.getMessage());
        }
    }

    private HttpHeaders createHeader(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return headers;
    }
}