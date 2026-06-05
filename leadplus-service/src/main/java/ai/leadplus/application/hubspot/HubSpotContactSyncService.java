package ai.leadplus.application.hubspot;

import ai.leadplus.application.tenantcontacts.TenantContactPayload;
import ai.leadplus.application.tenantcontacts.TenantContactService;
import ai.leadplus.domain.common.CRM;
import ai.leadplus.infrastructure.hubspot.auth.HubSpotAuthClient;
import ai.leadplus.infrastructure.hubspot.contacts.HubSpotContactAssociations;
import ai.leadplus.infrastructure.hubspot.contacts.HubSpotContactClient;
import ai.leadplus.infrastructure.hubspot.contacts.HubSpotContactResponse;
import ai.leadplus.infrastructure.hubspot.contacts.HubSpotContactsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubSpotContactSyncService {

    private final HubSpotContactClient hubSpotContactClient;
    private final TenantContactService tenantContactService;
    private final HubSpotAuthClient hubSpotAuthClient;

    public void syncContacts(String refreshToken, Long tenantId) {
        String after = null;
        do {
            String accessToken = hubSpotAuthClient.refreshAccessToken(refreshToken).getAccessToken();

            HubSpotContactsResponse response = hubSpotContactClient.fetchContacts(accessToken, after);

            if (response.getResults() == null) {
                log.warn("Empty HubSpot response");
                return;
            }

            List<HubSpotContactResponse> contacts = response.getResults();
            log.info("Fetched {} HubSpot contacts", contacts.size());

            List<TenantContactPayload> payloads = contacts.stream()
                    .map(contact -> TenantContactPayload.builder()
                            .sourceId(contact.getId())
                            .email(contact.getProperties().getEmail())
                            .firstName(contact.getProperties().getFirstName())
                            .lastName(contact.getProperties().getLastName())
                            .companyId(extractCompanyId(contact).orElse(null))
                            .build())
                    .toList();

            tenantContactService.saveAll(tenantId, CRM.HUBSPOT, payloads);

            after = response.getPaging() != null && response.getPaging().getNext() != null
                    ? response.getPaging().getNext().getAfter()
                    : null;
        } while (after != null);
    }

    private Optional<String> extractCompanyId(HubSpotContactResponse contact) {
        if (contact == null ||
                contact.getAssociations() == null ||
                contact.getAssociations().getCompanies() == null ||
                contact.getAssociations().getCompanies().getResults() == null) {
            return Optional.empty();
        }

        return contact.getAssociations()
                .getCompanies()
                .getResults()
                .stream()
                .map(HubSpotContactAssociations.Result::getId)
                .filter(Objects::nonNull)
                .distinct()
                .findFirst();
    }
}