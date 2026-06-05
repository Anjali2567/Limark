package ai.leadplus.domain.tenantannouncements;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantAnnouncementRepository extends JpaRepository<TenantAnnouncement, Long> {

    Page<TenantAnnouncement> findAllByTenantIdAndActiveTrue(Long tenantId, Pageable pageable);

    Page<TenantAnnouncement> findAllByTenantIdAndNameContainingIgnoreCaseAndActiveTrue(Long tenantId, String name, Pageable pageable);

    Optional<TenantAnnouncement> findByIdAndTenantIdAndActiveTrue(Long id, Long tenantId);

    List<TenantAnnouncement> findAllByStatusAndActiveTrue(TenantAnnouncementStatus status);
}
