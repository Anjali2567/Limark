package ai.leadplus.infrastructure.azure.graph.users;

import ai.leadplus.infrastructure.azure.AzureProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AzureUserClient {

    private final RestTemplate restTemplate;
    private final AzureProperties azureProperties;

    public AzureUserResponse getUserDetails(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = azureProperties.getGraphBaseUrl() + "/me";
        ResponseEntity<AzureUserResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, AzureUserResponse.class);

        log.info("Azure user details response status: {}", response.getStatusCode());
        return response.getBody();
    }
}
