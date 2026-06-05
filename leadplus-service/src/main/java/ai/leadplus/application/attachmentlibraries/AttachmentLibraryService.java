package ai.leadplus.application.attachmentlibraries;

import ai.leadplus.application.aws.AwsS3Service;
import ai.leadplus.application.common.utils.FileReader;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.attachmentlibraries.AttachmentLibrary;
import ai.leadplus.domain.attachmentlibraries.AttachmentLibraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttachmentLibraryService {

    private final AttachmentLibraryRepository attachmentLibraryRepository;
    private final AttachmentLibrarySearchService attachmentLibrarySearchService;
    private final AwsS3Service awsS3Service;

    public Page<AttachmentLibraryDto> searchResources(String workspaceId, String searchText, List<String> fileTypes, Pageable pageable) {
        Page<AttachmentLibrary> resources = attachmentLibrarySearchService.searchAttachmentLibraries(workspaceId, searchText, fileTypes, pageable);
        return resources.map(AttachmentLibraryDto::fromEntity);
    }

    public AttachmentLibraryDto getResourceById(String id) {
        AttachmentLibrary attachmentLibrary = findResourceById(id);
        return AttachmentLibraryDto.fromEntity(attachmentLibrary);
    }

    private AttachmentLibrary findResourceById(String id) {
        return attachmentLibraryRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new ResourceNotFoundException("Attachment Library not found with id: " + id));
    }

    public AttachmentLibraryDto createResource(String workspaceId, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String fileName = file.getOriginalFilename();
        String baseName = fileName != null ? fileName.replaceFirst("\\.[^.]+$", "") : "";
        String fileType = FileReader.getFileExtension(fileName);

        AttachmentLibrary attachmentLibrary = AttachmentLibrary.builder()
                .workspaceId(Long.parseLong(workspaceId))
                .filename(baseName)
                .sizeBytes(file.getSize())
                .fileType(fileType)
                .build();

        AttachmentLibrary savedResource = attachmentLibraryRepository.save(attachmentLibrary);
        String resourceUrl = awsS3Service.uploadFile(file, "attachment-libraries", String.valueOf(savedResource.getId()));
        savedResource.setFileUrl(resourceUrl);
        return AttachmentLibraryDto.fromEntity(attachmentLibraryRepository.save(savedResource));
    }

    public void deleteResource(String id) {
        AttachmentLibrary attachmentLibrary = findResourceById(id);
        awsS3Service.deleteExistingFile(attachmentLibrary.getFileUrl());
        attachmentLibraryRepository.delete(attachmentLibrary);
    }

    public List<AttachmentLibraryDto> getAttachmentsByIds(List<String> ids) {
        List<Long> longIds = ids.stream().map(Long::parseLong).toList();
        List<AttachmentLibrary> resources = attachmentLibraryRepository.findAllByIdIn(longIds);
        return resources.stream()
                .map(AttachmentLibraryDto::fromEntity)
                .toList();
    }

    public AttachmentLibraryDto renameAttachmentLibraryById(String id, String fileName) {
        AttachmentLibrary attachmentLibrary = findResourceById(id);
        attachmentLibrary.setFilename(fileName);
        return AttachmentLibraryDto.fromEntity(attachmentLibraryRepository.save(attachmentLibrary));
    }

    public AttachmentLibraryDto replaceAttachmentLibraryFile(String id, MultipartFile file) {
        AttachmentLibrary attachmentLibrary = findResourceById(id);

        String fileName = file.getOriginalFilename();
        String baseName = fileName != null ? fileName.replaceFirst("\\.[^.]+$", "") : "";
        String fileType = FileReader.getFileExtension(fileName);

        attachmentLibrary.setFilename(baseName);
        attachmentLibrary.setSizeBytes(file.getSize());
        attachmentLibrary.setFileType(fileType);

        String resourceURl = awsS3Service.uploadFile(file, "attachment-libraries", String.valueOf(attachmentLibrary.getId()));
        attachmentLibrary.setFileUrl(resourceURl);
        return AttachmentLibraryDto.fromEntity(attachmentLibraryRepository.save(attachmentLibrary));
    }
}
