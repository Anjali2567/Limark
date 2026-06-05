package ai.leadplus.application.tenantcompanies;

import ai.leadplus.domain.common.CRM;
import ai.leadplus.domain.tenantcompanies.TenantCompany;
import ai.leadplus.domain.tenantcompanies.TenantCompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantCompanyService {

    private final TenantCompanyRepository tenantCompanyRepository;

    public void saveAll(Long tenantId, CRM sourceType, List<TenantCompanyPayload> payloads) {
        List<String> sourceIds = payloads.stream()
                .map(TenantCompanyPayload::getSourceId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        if (sourceIds.isEmpty()) {
            log.info("No valid payloads to save for tenant {}", tenantId);
            return;
        }

        Set<String> existingSourceIds = tenantCompanyRepository
                .findAllByTenantIdAndSourceTypeAndSourceIdIn(tenantId, sourceType, sourceIds)
                .stream()
                    .map(TenantCompany::getSourceId).collect(Collectors.toSet());

        List<TenantCompany> newCompanies = payloads.stream()
                .filter(p -> p.getSourceId() != null)
                .filter(p -> !existingSourceIds.contains(p.getSourceId()))
                .map(p -> TenantCompany.builder()
                        .tenantId(tenantId)
                        .sourceType(sourceType)
                        .sourceId(p.getSourceId())
                        .name(p.getName())
                        .build())
                .toList();

        if (!newCompanies.isEmpty()) {
            tenantCompanyRepository.saveAll(newCompanies);
        }

        log.info("Saved {} new {} companies for tenant {}", newCompanies.size(), sourceType, tenantId);
    }
}
