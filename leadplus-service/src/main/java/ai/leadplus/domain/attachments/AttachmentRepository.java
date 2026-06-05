package ai.leadplus.domain.attachments;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    Optional<Attachment> findByIdAndSourceTypeAndActiveTrue(Long id, SourceType sourceType);

    List<Attachment> findAllBySourceIdAndSourceTypeAndActiveTrue(Long sourceId, SourceType sourceType);

    List<Attachment> findAllBySourceIdInAndSourceTypeAndActiveTrue(List<Long> sourceIds, SourceType sourceType);
}
