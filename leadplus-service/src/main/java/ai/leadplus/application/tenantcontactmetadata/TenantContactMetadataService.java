package ai.leadplus.application.tenantcontactmetadata;

import ai.leadplus.domain.tenantcontactmetadata.TenantContactMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TenantContactMetadataService {

    private final TenantContactMetadataRepository tenantContactMetadataRepository;

    public Map<String, List<String>> getAllDistinctValues(Long tenantId) {
        return Map.of(
                "bdName", tenantContactMetadataRepository.findDistinctBdNames(tenantId),
                "isrName", tenantContactMetadataRepository.findDistinctIsrNames(tenantId),
                "priority", tenantContactMetadataRepository.findDistinctPriorities(tenantId),
                "titleCategory", tenantContactMetadataRepository.findDistinctTitleCategories(tenantId)
        );
    }
}
