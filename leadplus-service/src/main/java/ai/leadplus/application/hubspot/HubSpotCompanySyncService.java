package ai.leadplus.application.hubspot;

import ai.leadplus.application.tenantcompanies.TenantCompanyPayload;
import ai.leadplus.application.tenantcompanies.TenantCompanyService;
import ai.leadplus.domain.common.CRM;
import ai.leadplus.infrastructure.hubspot.auth.HubSpotAuthClient;
import ai.leadplus.infrastructure.hubspot.companies.HubSpotCompaniesResponse;
import ai.leadplus.infrastructure.hubspot.companies.HubSpotCompanyClient;
import ai.leadplus.infrastructure.hubspot.companies.HubSpotCompanyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubSpotCompanySyncService {

    private final HubSpotCompanyClient hubSpotCompanyClient;
    private final TenantCompanyService tenantCompanyService;
    private final HubSpotAuthClient hubSpotAuthClient;

    public void syncCompanies(String refreshToken, Long tenantId) {
        String after = null;
        do {
            String accessToken = hubSpotAuthClient.refreshAccessToken(refreshToken).getAccessToken();

            HubSpotCompaniesResponse response = hubSpotCompanyClient.fetchCompanies(accessToken, after);

            if (response.getResults() == null) {
                log.warn("Empty HubSpot companies response");
                return;
            }

            List<HubSpotCompanyResponse> companies = response.getResults();
            log.info("Fetched {} HubSpot companies", companies.size());

            List<TenantCompanyPayload> payloads = companies.stream()
                    .map(company -> TenantCompanyPayload.builder()
                            .sourceId(company.getId())
                            .name(company.getProperties() != null ? company.getProperties().getName() : null)
                            .build())
                    .toList();

            tenantCompanyService.saveAll(tenantId, CRM.HUBSPOT, payloads);

            after = response.getPaging() != null && response.getPaging().getNext() != null
                    ? response.getPaging().getNext().getAfter()
                    : null;
        } while (after != null);
    }
}
