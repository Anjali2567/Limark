package ai.leadplus.application.zoho;

import ai.leadplus.application.tenantcontacts.TenantContactPayload;
import ai.leadplus.application.tenantcontacts.TenantContactService;
import ai.leadplus.domain.common.CRM;
import ai.leadplus.infrastructure.zoho.contacts.ZohoContactClient;
import ai.leadplus.infrastructure.zoho.contacts.ZohoContactResponse;
import ai.leadplus.infrastructure.zoho.contacts.ZohoContactsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZohoContactSyncService {

    private final ZohoContactClient zohoContactClient;
    private final TenantContactService tenantContactService;

    private static final int PER_PAGE = 100;

    public void syncContacts(String refreshToken, Long tenantId) {

        int page = 1;
        boolean moreRecords;

        do {
            ZohoContactsResponse response = zohoContactClient.fetchContacts(refreshToken, page, PER_PAGE);

            if (response.getData() == null) {
                log.warn("Empty Zoho response at page {}", page);
                return;
            }

            List<ZohoContactResponse> contacts = response.getData();
            log.info("Fetched {} Zoho contacts at page {}", contacts.size(), page);

            List<TenantContactPayload> payloads = contacts.stream()
                    .map(contact -> {
                        String companyId = (contact.getAccountName() != null) ? contact.getAccountName().getId() : null;
                        return TenantContactPayload.builder()
                                .sourceId(contact.getId())
                                .email(contact.getEmail())
                                .firstName(contact.getFirstName())
                                .lastName(contact.getLastName())
                                .companyId(companyId)
                                .build();
                    })
                    .toList();
            tenantContactService.saveAll(tenantId, CRM.ZOHO, payloads);

            moreRecords = response.getInfo() != null && response.getInfo().isMoreRecords();
            page++;

        } while (moreRecords);
    }
}