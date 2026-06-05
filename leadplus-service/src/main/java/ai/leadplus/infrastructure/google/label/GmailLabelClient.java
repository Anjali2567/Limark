package ai.leadplus.infrastructure.google.label;

import ai.leadplus.application.exception.ServiceUnavailableException;
import ai.leadplus.infrastructure.google.auth.GoogleAuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailLabelClient {

    private static final String LABEL_NAME = "LeadPlus";
    private static final String LABEL_URL = "https://gmail.googleapis.com/gmail/v1/users/me/labels";
    private static final String MESSAGE_MODIFY_URL = "https://gmail.googleapis.com/gmail/v1/users/me/messages/{messageId}/modify";


    private final GoogleAuthClient googleAuthClient;
    private final RestTemplate restTemplate;

    public String getOrCreateLeadPlusLabelId(String accessToken) {
        try {
            HttpHeaders headers = googleAuthClient.createHeader(accessToken);

            GmailLabelListResponse listResponse = getGmailLabelList(headers);
            Optional<String> existingId = findLabelId(listResponse.getLabels());

            if (existingId.isPresent()) {
                log.info("Found existing LeadPlus label: {}", existingId.get());
                return existingId.get();
            }

            log.info("Creating new LeadPlus label");
            GmailLabelResponse created = createGmailLabel(headers);
            log.info("Created LeadPlus label: {}", created.getId());
            return created.getId();
        } catch (Exception e) {
            log.error("Could not create LeadPlus label: {}", e.getMessage());
            return null;
        }
    }

    public void appendLabelToMessage(String accessToken, String messageId, String labelId) {
        try {
            HttpHeaders headers = googleAuthClient.createHeader(accessToken);

            GmailAppendLabelRequest request = new GmailAppendLabelRequest(List.of(labelId));
            HttpEntity<GmailAppendLabelRequest> entity = new HttpEntity<>(request, headers);

            String url = MESSAGE_MODIFY_URL.replace("{messageId}", messageId);
            restTemplate.postForObject(url, entity, Void.class);
        }
        catch (Exception e) {
            log.error("Could not append label to messageId: {}", messageId);
        }
    }

    private GmailLabelListResponse getGmailLabelList(HttpHeaders headers) {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<GmailLabelListResponse> response = restTemplate.exchange(
                LABEL_URL,
                HttpMethod.GET,
                entity,
                GmailLabelListResponse.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to list labels: {}", response.getStatusCode());
            throw new ServiceUnavailableException("Failed to list labels");
        }
        return response.getBody();
    }

    private GmailLabelResponse createGmailLabel(HttpHeaders headers) {
        GmailCreateLabelRequest request = new GmailCreateLabelRequest(LABEL_NAME);
        HttpEntity<GmailCreateLabelRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<GmailLabelResponse> response = restTemplate.postForEntity(
                LABEL_URL,
                entity,
                GmailLabelResponse.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to create label: {}", response.getStatusCode());
            throw new ServiceUnavailableException("Could not create label");
        }
        return response.getBody();
    }

    private Optional<String> findLabelId(List<GmailLabelResponse> labels) {
        return labels.stream()
                .filter(label -> GmailLabelClient.LABEL_NAME.equalsIgnoreCase(label.getName()))
                .map(GmailLabelResponse::getId)
                .findFirst();
    }
}
