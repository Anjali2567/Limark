package ai.leadplus.domain.leadnotes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeadNoteRepository extends JpaRepository<LeadNote, Long> {

    Optional<LeadNote> findByIdAndTenantIdAndWorkspaceIdAndActiveTrue(Long id, Long tenantId, Long workspaceId);

    Page<LeadNote> findAllByTenantIdAndWorkspaceIdAndSourceIdAndActiveTrue(Long tenantId, Long workspaceId, Long sourceId, Pageable pageable);
}

