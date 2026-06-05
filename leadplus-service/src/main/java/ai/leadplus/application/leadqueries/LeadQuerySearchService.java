package ai.leadplus.application.leadqueries;

import ai.leadplus.domain.leadqueries.LeadQuery;
import ai.leadplus.domain.leadqueries.LeadQueryRepository;
import ai.leadplus.domain.leadqueries.LeadQueryType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeadQuerySearchService {

    private final LeadQueryRepository leadQueryRepository;

    public Page<LeadQuery> searchLeadQueries(String searchText, List<LeadQueryType> types, Pageable pageable) {
        Specification<LeadQuery> spec = Specification.where(null);

        spec = spec.and((root, query, cb) -> root.get("type").in(types));

        if (searchText != null && !searchText.trim().isEmpty()) {
            String searchPattern = "%" + searchText.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("value")), searchPattern)
            );
        }

        return leadQueryRepository.findAll(spec, pageable);
    }
}
