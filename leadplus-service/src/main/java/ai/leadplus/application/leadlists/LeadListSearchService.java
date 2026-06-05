package ai.leadplus.application.leadlists;

import ai.leadplus.domain.common.LeadType;
import ai.leadplus.domain.leadlists.LeadList;
import ai.leadplus.domain.leadlists.LeadListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LeadListSearchService {

    private final LeadListRepository leadListRepository;

    public Page<LeadList> searchLeadLists(String tenantId, String workspaceId, String searchText, LeadType type, Pageable pageable) {
        Specification<LeadList> spec = Specification.where(null);

        if (searchText != null && !searchText.trim().isEmpty()) {
            String[] searchWords = searchText.trim().split("\\s+");
            for (String word : searchWords) {
                String searchPattern = "%" + word.toLowerCase() + "%";
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("name")), searchPattern)
                );
            }
        }

        if (StringUtils.hasText(tenantId)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tenantId"), tenantId));
        }

        if (StringUtils.hasText(workspaceId)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("workspaceId"), workspaceId));
        }

        if (type != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type));
        }

        spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), true));

        return leadListRepository.findAll(spec, pageable);
    }
}
