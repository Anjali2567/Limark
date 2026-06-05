package ai.leadplus.infrastructure.google.userinfo;

import ai.leadplus.application.exception.InvalidOAuthLoginException;
import ai.leadplus.application.exception.ServiceUnavailableException;
import ai.leadplus.infrastructure.google.GoogleProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleUserInfoClient {

    private static final String GOOGLE_TOKEN_INFO_URL = "https://www.googleapis.com/oauth2/v3/tokeninfo";

    private final RestTemplate restTemplate;
    private final GoogleProperties googleProperties;

    public GoogleUserInfoResponse getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = "https://www.googleapis.com/oauth2/v1/userinfo";
        ResponseEntity<GoogleUserInfoResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                GoogleUserInfoResponse.class
        );
        log.info("Google user info response status: {}", response.getStatusCode());

        GoogleUserInfoResponse body = response.getBody();
        if (body == null) {
            log.error("Google user info response body is null for status: {}", response.getStatusCode());
            throw new ServiceUnavailableException("Google user info response body is null");
        }
        return body;
    }

    public boolean verifyTokenOrigin(String accessToken, String expectedClientId) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(GOOGLE_TOKEN_INFO_URL)
                    .queryParam("access_token", accessToken)
                    .build(true)
                    .toUri();

            ResponseEntity<GoogleTokenInfoResponse> response =
                    restTemplate.getForEntity(uri, GoogleTokenInfoResponse.class);

            return response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && expectedClientId.equals(response.getBody().getAud());

        } catch (Exception ex) {
            log.error("Error verifying token origin", ex);
            return false;
        }
    }

    public GoogleUserInfoResponse getVerifiedUserInfo(String accessToken) {
        if (!verifyTokenOrigin(accessToken, googleProperties.getClientId())) {
            throw new InvalidOAuthLoginException("Invalid token origin");
        }
        return getUserInfo(accessToken);
    }
}
