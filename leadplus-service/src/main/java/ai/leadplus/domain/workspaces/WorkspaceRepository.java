package ai.leadplus.domain.workspaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    int countWorkspacesByOwnerId(Long ownerId);

    Page<Workspace> findByTenantIdAndNameContainingIgnoreCase(Long tenantId, String query, Pageable pageable);

    Page<Workspace> findByTenantId(Long tenantId, Pageable pageable);
}
