package ai.leadplus.domain.tenantannouncementcontacts;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TenantAnnouncementContactRepository extends JpaRepository<TenantAnnouncementContact, Long> {

    Page<TenantAnnouncementContact> findAllByAnnouncementId(Long announcementId, Pageable pageable);

    Page<TenantAnnouncementContact> findAllByAnnouncementIdAndStatus(Long announcementId, TenantAnnouncementContactStatus status, Pageable pageable);

    List<TenantAnnouncementContact> findAllByAnnouncementIdAndEmailIn(Long announcementId, List<String> emails);

    long countByAnnouncementId(Long announcementId);

    long countByAnnouncementIdAndStatus(Long announcementId, TenantAnnouncementContactStatus status);

    long countByAnnouncementIdInAndSentAtAfter(List<Long> announcementIds, LocalDateTime after);

    void deleteAllByAnnouncementId(Long announcementId);

    Optional<TenantAnnouncementContact> findByIdAndAnnouncementId(Long contactId, Long announcementId);
}
