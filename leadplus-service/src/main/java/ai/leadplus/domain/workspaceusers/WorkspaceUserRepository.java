package ai.leadplus.domain.workspaceusers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface WorkspaceUserRepository extends JpaRepository<WorkspaceUser, Long> {

    Optional<WorkspaceUser> findByWorkspaceIdAndUserIdAndActiveTrue(Long workspaceId, Long userId);

    Page<WorkspaceUser> findByWorkspaceIdAndActiveTrue(Long workspaceId, Pageable pageable);

    boolean existsByWorkspaceIdAndUserIdAndActiveTrue(Long workspaceId, Long userId);

    List<WorkspaceUser> findByUserIdAndActiveTrue(Long userId);

    default List<WorkspaceUser> findActiveWorkspaceUsersByUserId(Long userId) {
        return findByUserIdAndActiveTrue(userId).stream()
            .filter(u -> u.getWorkspaceUserStatus() == WorkspaceUserStatus.ACCEPTED || 
                        u.getWorkspaceUserRole() == WorkspaceUserRole.OWNER)
            .collect(Collectors.toList());
    }

    Optional<WorkspaceUser> findByInvitationToken(String token);

    List<WorkspaceUser> findAllByUserIdInAndActiveTrue(Set<Long> userIds);

    List<WorkspaceUser> findAllByWorkspaceIdInAndActiveTrue(List<Long> userIds);

    long countByTenantIdAndActiveTrue(Long tenantId);
}
