package ai.leadplus.infrastructure.zoho.user;

import ai.leadplus.application.exception.ServiceUnavailableException;
import ai.leadplus.infrastructure.zoho.ZohoConfiguration;
import ai.leadplus.infrastructure.zoho.auth.ZohoAuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZohoUserClient {

    private final ZohoConfiguration zohoConfiguration;
    private final ZohoAuthClient zohoAuthClient;
    private final RestTemplate restTemplate;

    public ZohoUserResponse getUserInfo(String refreshToken) {
        String url = zohoConfiguration.getAccountsDomain() + "/oauth/user/info";
        HttpEntity<Void> request = new HttpEntity<>(zohoAuthClient.createHeader(refreshToken));

        try {
            ResponseEntity<ZohoUserResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    ZohoUserResponse.class
            );
            if (response.getBody() == null || StringUtils.hasText(response.getBody().getError())) {
                log.error("Failed to fetch Zoho user info: empty response body");
                throw new ServiceUnavailableException("Failed to fetch Zoho user info: empty response body");
            }
            log.info("Fetched Zoho user info successfully");
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch Zoho user info", e);
            throw new ServiceUnavailableException("Failed to fetch Zoho user info: " + e.getMessage());
        }
    }
}
