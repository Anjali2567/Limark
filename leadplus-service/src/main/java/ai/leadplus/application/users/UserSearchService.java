package ai.leadplus.application.users;

import ai.leadplus.domain.users.User;
import ai.leadplus.domain.users.UserRepository;
import ai.leadplus.domain.users.UserRole;
import ai.leadplus.domain.users.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSearchService {

    private final UserRepository userRepository;

    public Page<User> searchUsers(String tenantId, String searchText, List<UserRole> userRoles, List<UserStatus> userStatuses, Pageable pageable) {
        Specification<User> spec = Specification.where(null);

        // Text search on name and email (case-insensitive substring match)
        if (searchText != null && !searchText.trim().isEmpty()) {
            String[] searchWords = searchText.trim().split("\\s+");
            for (String word : searchWords) {
                String searchPattern = "%" + word.replace("+", "\\+") + "%";
                spec = spec.and((root, query, cb) -> 
                    cb.or(
                        cb.like(cb.lower(root.get("name")), searchPattern.toLowerCase()),
                        cb.like(cb.lower(root.get("email")), searchPattern.toLowerCase())
                    )
                );
            }
        }

        // Filter by tenant
        if (StringUtils.hasText(tenantId)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tenantId"), tenantId));
        }

        // Filter by roles
        if (!CollectionUtils.isEmpty(userRoles)) {
            spec = spec.and((root, query, cb) -> root.get("roles").in(userRoles));
        }

        // Filter by status - note: assuming UserStatus is stored as a collection or single field
        if (!CollectionUtils.isEmpty(userStatuses)) {
            // If status is a single field, use simple in() predicate
            spec = spec.and((root, query, cb) -> {
                if (userStatuses.size() == 1) {
                    return cb.equal(root.get("status"), userStatuses.get(0));
                }
                return root.get("status").in(userStatuses);
            });
        }

        // Only active users
        spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), true));

        return userRepository.findAll(spec, pageable);
    }
}
