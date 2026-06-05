package ai.leadplus.infrastructure.google.auth;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.MailboxTokenExpiredException;
import ai.leadplus.application.exception.ServiceUnavailableException;
import ai.leadplus.infrastructure.google.GoogleProperties;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthClient {

    private final GoogleProperties googleProperties;
    private final RestTemplate restTemplate;

    public GoogleTokenResponse fetchTokensFromCode(String code, String redirectUri) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", googleProperties.getClientId());
        formData.add("client_secret", googleProperties.getClientSecret());
        formData.add("code", code);
        formData.add("redirect_uri", redirectUri);
        formData.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(tokenUrl, request, GoogleTokenResponse.class);

        if (response.getBody() == null) {
            throw new ServiceUnavailableException("Failed to fetch access token and refresh token");
        }

        GoogleTokenResponse tokenResponse = response.getBody();
        validateScopes(tokenResponse.getScope());

        log.info("Google access token and refresh token created");
        return response.getBody();
    }

    private void validateScopes(String scopeString) {

        if (!StringUtils.hasText(scopeString)) {
            throw new BadRequestException("Google authentication failed. Please grant all required permissions and try again.");
        }

        Set<String> responseScopes = Arrays.stream(scopeString.split(" "))
                .map(String::trim)
                .collect(Collectors.toSet());

        List<String> requiredScopes = googleProperties.getRequiredScopes();

        List<String> missingScopes = requiredScopes.stream()
                .filter(required -> !responseScopes.contains(required))
                .toList();

        if (!missingScopes.isEmpty()) {
            throw new BadRequestException("Google authentication failed. Please grant all required permissions and try again.");
        }
    }

    public GoogleTokenResponse fetchTokensFromRefreshToken(String refreshToken) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", googleProperties.getClientId());
        formData.add("client_secret", googleProperties.getClientSecret());
        formData.add("refresh_token", refreshToken);
        formData.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        ResponseEntity<GoogleTokenResponse> response;
        try {
            response = restTemplate.postForEntity(tokenUrl, request, GoogleTokenResponse.class);
        } catch (HttpClientErrorException e) {
            if (e.getResponseBodyAsString().contains("invalid_grant")) {
                throw new MailboxTokenExpiredException("Google refresh token is invalid or expired", e);
            }
            throw e;
        }

        if (response.getBody() == null) {
            throw new ServiceUnavailableException("Failed to fetch access token from refresh token");
        }

        log.info("Google access token created from refresh token");
        return response.getBody();
    }

    public HttpHeaders createHeader(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

}

