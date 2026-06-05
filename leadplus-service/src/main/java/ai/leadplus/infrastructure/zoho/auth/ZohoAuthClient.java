package ai.leadplus.infrastructure.zoho.auth;


import ai.leadplus.application.exception.ServiceUnavailableException;
import ai.leadplus.infrastructure.zoho.ZohoConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZohoAuthClient {

    private final ZohoConfiguration zohoConfiguration;
    private final RestTemplate restTemplate;

    public ZohoRefreshTokenResponse getTokenFromCode(String code, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", zohoConfiguration.getClientId());
        formData.add("client_secret", zohoConfiguration.getClientSecret());
        formData.add("code", code);
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        String tokenUrl = zohoConfiguration.getAccountsDomain() + "/oauth/v2/token";
        ResponseEntity<ZohoRefreshTokenResponse> response = restTemplate.postForEntity(tokenUrl, request,
                ZohoRefreshTokenResponse.class);
        if (response.getBody() == null || StringUtils.hasText(response.getBody().getError())) {
            throw new ServiceUnavailableException("Failed to fetch refresh token");
        }
        log.info("Fetched Zoho refresh token successfully");
        return response.getBody();
    }

    public String fetchAccessToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", zohoConfiguration.getClientId());
        formData.add("client_secret", zohoConfiguration.getClientSecret());
        formData.add("refresh_token", refreshToken);
        formData.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        String tokenUrl = zohoConfiguration.getAccountsDomain() + "/oauth/v2/token";
        ResponseEntity<ZohoAccessTokenResponse> response = restTemplate.postForEntity(tokenUrl, request,
                ZohoAccessTokenResponse.class);
        if (response.getBody() == null || StringUtils.hasText(response.getBody().getError())) {
            throw new ServiceUnavailableException("Failed to fetch access token");
        }
        log.info("Fetched Zoho access token successfully");
        return response.getBody().getAccessToken();
    }

    public HttpHeaders createHeader(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Zoho-oauthtoken " + fetchAccessToken(refreshToken));
        return headers;
    }
}