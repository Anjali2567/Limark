package ai.leadplus.infrastructure.azure.auth;

import ai.leadplus.application.exception.MailboxTokenExpiredException;
import ai.leadplus.application.exception.ServiceUnavailableException;
import ai.leadplus.infrastructure.azure.AzureProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AzureAuthClient {

    private final AzureProperties azureProperties;
    private final RestTemplate restTemplate;

    public AzureRefreshTokenResponse fetchAccessTokenAndRefreshTokenFromCode(String code, String redirectUri) {
        String tokenUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", azureProperties.getClientId());
        formData.add("client_secret", azureProperties.getClientSecret());
        formData.add("scope", azureProperties.getAuthentication().getScope());
        formData.add("code", code);
        formData.add("redirect_uri", redirectUri);
        formData.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        ResponseEntity<AzureRefreshTokenResponse> response = restTemplate.postForEntity(tokenUrl, request, AzureRefreshTokenResponse.class);
        if (response.getBody() == null) {
            throw new ServiceUnavailableException("Failed to fetch access token and refresh token");
        }
        log.info("Azure access token and refresh token created at: {}", LocalDateTime.now());
        return response.getBody();
    }

    public AzureRefreshTokenResponse fetchAccessTokenAndRefreshTokenFromRefreshToken(String refreshToken) {
        String tokenUrl = azureProperties.getAuthentication().getUrl() + "/common/oauth2/v2.0/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", azureProperties.getClientId());
        formData.add("client_secret", azureProperties.getClientSecret());
        formData.add("scope", azureProperties.getAuthentication().getScope());
        formData.add("refresh_token", refreshToken);
        formData.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        ResponseEntity<AzureRefreshTokenResponse> response;
        try {
            response = restTemplate.postForEntity(tokenUrl, request, AzureRefreshTokenResponse.class);
        } catch (HttpClientErrorException e) {
            if (e.getResponseBodyAsString().contains("invalid_grant")) {
                throw new MailboxTokenExpiredException("Azure refresh token is invalid or expired", e);
            }
            throw e;
        }
        if (response.getBody() == null) {
            throw new ServiceUnavailableException("Failed to fetch access token and refresh token from refresh token");
        }
        log.info("Azure access token and refresh token created at: {} from refresh token", LocalDateTime.now());
        return response.getBody();
    }

    public HttpHeaders createHeader(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public String createUserUrl(String userId) {
        return azureProperties.getGraphBaseUrl() + "/users/" + userId;
    }
}
