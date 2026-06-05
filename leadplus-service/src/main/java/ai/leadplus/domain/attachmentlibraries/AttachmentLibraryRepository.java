package ai.leadplus.domain.attachmentlibraries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AttachmentLibraryRepository extends JpaRepository<AttachmentLibrary, Long>, JpaSpecificationExecutor<AttachmentLibrary> {

    List<AttachmentLibrary> findAllByIdIn(List<Long> ids);
}
