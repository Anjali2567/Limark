package ai.leadplus.application.zoho;

import ai.leadplus.application.tenantcompanies.TenantCompanyPayload;
import ai.leadplus.application.tenantcompanies.TenantCompanyService;
import ai.leadplus.domain.common.CRM;
import ai.leadplus.infrastructure.zoho.accounts.ZohoCompaniesResponse;
import ai.leadplus.infrastructure.zoho.accounts.ZohoCompanyClient;
import ai.leadplus.infrastructure.zoho.accounts.ZohoCompanyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZohoCompanySyncService {

    private final ZohoCompanyClient zohoCompanyClient;
    private final TenantCompanyService tenantCompanyService;

    private static final int PER_PAGE = 100;

    public void syncCompanies(String refreshToken, Long tenantId) {
        int page = 1;
        boolean moreRecords;

        do {
            ZohoCompaniesResponse response = zohoCompanyClient.fetchCompanies(refreshToken, page, PER_PAGE);

            if (response.getData() == null) {
                log.warn("Empty Zoho companies response at page {}", page);
                return;
            }

            List<ZohoCompanyResponse> companies = response.getData();
            log.info("Fetched {} Zoho companies at page {}", companies.size(), page);

            List<TenantCompanyPayload> payloads = companies.stream()
                    .map(company -> TenantCompanyPayload.builder()
                            .sourceId(company.getId())
                            .name(company.getName())
                            .build())
                    .toList();

            tenantCompanyService.saveAll(tenantId, CRM.ZOHO, payloads);

            moreRecords = response.getInfo() != null && response.getInfo().isMoreRecords();
            page++;

        } while (moreRecords);
    }
}
