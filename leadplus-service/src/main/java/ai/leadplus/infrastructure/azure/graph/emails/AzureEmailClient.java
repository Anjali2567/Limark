package ai.leadplus.infrastructure.azure.graph.emails;

import ai.leadplus.application.exception.ServiceUnavailableException;
import ai.leadplus.infrastructure.azure.auth.AzureAuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AzureEmailClient {

    private final AzureAuthClient azureAuthClient;
    private final RestTemplate restTemplate;

    public AzureGraphMessageResponse createDraftEmail(AzureEmailSendRequest request, String userId, String accessToken) {
        log.info("Sending draft email on behalf of userId={}", userId);
        HttpHeaders headers = azureAuthClient.createHeader(accessToken);
        HttpEntity<AzureEmailSendRequest> entity = new HttpEntity<>(request, headers);

        String url = createUserMessagesUrl(userId);
        ResponseEntity<AzureGraphMessageResponse> response = restTemplate.postForEntity(url, entity, AzureGraphMessageResponse.class);
        log.info("Send draft email response status: {}", response.getStatusCode());
        return response.getBody();
    }

    public void sendDraftedEmail(String userId, String messageId, String accessToken) {
        log.info("Sending drafted email on behalf of userId={} and messageId={}", userId, messageId);
        HttpHeaders headers = azureAuthClient.createHeader(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = createUserSendDraftedMailUrl(userId, messageId);
        ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);
        log.info("Send drafted email response status: {}", response.getStatusCode());
    }

    public AzureConversationResponse getConversationById(
            String userId,
            String conversationId,
            String accessToken
    ) {
        log.info("Fetching Outlook conversationId={} for userId={}", conversationId, userId);

        HttpHeaders headers = azureAuthClient.createHeader(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url =
                azureAuthClient.createUserUrl(userId)
                        + "/messages"
                        + "?$filter=conversationId eq '" + conversationId + "'"
                        + "&$top=25";

        ResponseEntity<AzureConversationResponse> response =
                restTemplate.exchange(
                        url,
                        org.springframework.http.HttpMethod.GET,
                        entity,
                        AzureConversationResponse.class
                );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new ServiceUnavailableException("Failed to fetch Outlook conversation");
        }

        log.info("Fetched {} messages for conversationId={}",
                response.getBody().getValue().size(),
                conversationId
        );

        return response.getBody();
    }


    private String createUserMessagesUrl(String userId) {
        return azureAuthClient.createUserUrl(userId) + "/messages";
    }

    private String createUserMessagesUrl(String userId, String messageId) {
        return createUserMessagesUrl(userId) + "/" + messageId;
    }

    private String createUserSendDraftedMailUrl(String userId, String messageId) {
        return createUserMessagesUrl(userId, messageId) + "/send";
    }
}
