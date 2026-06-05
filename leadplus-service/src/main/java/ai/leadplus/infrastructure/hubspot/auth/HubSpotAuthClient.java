package ai.leadplus.infrastructure.hubspot.auth;

import ai.leadplus.application.exception.ServiceUnavailableException;
import ai.leadplus.infrastructure.hubspot.HubSpotConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubSpotAuthClient {

    private final HubSpotConfiguration hubSpotConfiguration;
    private final RestTemplate restTemplate;

    public HubSpotAccessTokenResponse getTokenFromCode(String code, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", hubSpotConfiguration.getClientId());
        formData.add("client_secret", hubSpotConfiguration.getClientSecret());
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        ResponseEntity<HubSpotAccessTokenResponse> response = restTemplate.postForEntity(
                "https://api.hubapi.com/oauth/v1/token",
                request,
                HubSpotAccessTokenResponse.class
        );

        if (response.getBody() == null || StringUtils.hasText(response.getBody().getError())) {
            throw new ServiceUnavailableException("Failed to fetch HubSpot token");
        }

        log.info("Fetched HubSpot token successfully");
        return response.getBody();
    }

    public HubSpotAccessTokenResponse refreshAccessToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", hubSpotConfiguration.getClientId());
        formData.add("client_secret", hubSpotConfiguration.getClientSecret());
        formData.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        ResponseEntity<HubSpotAccessTokenResponse> response = restTemplate.postForEntity(
                "https://api.hubapi.com/oauth/v1/token",
                request,
                HubSpotAccessTokenResponse.class
        );

        if (response.getBody() == null || StringUtils.hasText(response.getBody().getError())) {
            throw new ServiceUnavailableException("Failed to refresh HubSpot access token");
        }

        log.info("Refreshed HubSpot access token successfully");
        return response.getBody();
    }

    public HttpHeaders createHeader(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return headers;
    }
}