package ai.leadplus.application.attachmentlibraries;


import ai.leadplus.domain.attachmentlibraries.AttachmentLibrary;
import ai.leadplus.domain.attachmentlibraries.AttachmentLibraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttachmentLibrarySearchService {

    private final AttachmentLibraryRepository attachmentLibraryRepository;

    public Page<AttachmentLibrary> searchAttachmentLibraries(String workspaceId, String searchText, List<String> fileTypes, Pageable pageable) {
        Specification<AttachmentLibrary> spec = Specification.where(null);

        spec = spec.and((root, query, cb) -> cb.equal(root.get("workspaceId"), workspaceId));

        if (searchText != null && !searchText.trim().isEmpty()) {
            String searchPattern = "%" + searchText.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("filename")), searchPattern)
            );
        }

        if (!CollectionUtils.isEmpty(fileTypes)) {
            spec = spec.and((root, query, cb) -> {
                return cb.or(fileTypes.stream()
                        .map(type -> cb.equal(cb.lower(root.get("fileType")), type.toLowerCase()))
                        .toArray(jakarta.persistence.criteria.Predicate[]::new));
            });
        }

        return attachmentLibraryRepository.findAll(spec, pageable);
    }
}