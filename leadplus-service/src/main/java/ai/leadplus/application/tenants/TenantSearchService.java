package ai.leadplus.application.tenants;

import ai.leadplus.domain.tenants.Tenant;
import ai.leadplus.domain.tenants.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantSearchService {

    private final TenantRepository tenantRepository;

    public Page<Tenant> searchTenants(String searchText, Pageable pageable) {
        Specification<Tenant> spec = Specification.where(null);

        if (searchText != null && !searchText.trim().isEmpty()) {
            String[] words = searchText.trim().split("\\s+");
            for (String word : words) {
                String searchPattern = "%" + word.toLowerCase() + "%";
                spec = spec.and((root, query, cb) ->
                        cb.or(
                                cb.like(cb.lower(root.get("name")), searchPattern),
                                cb.like(cb.lower(root.get("domain")), searchPattern)
                        )
                );
            }
        }

        return tenantRepository.findAll(spec, pageable);
    }
}
