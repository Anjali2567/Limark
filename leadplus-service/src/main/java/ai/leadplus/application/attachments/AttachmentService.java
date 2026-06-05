package ai.leadplus.application.attachments;

import ai.leadplus.application.aws.AwsS3Service;
import ai.leadplus.application.common.utils.FileReader;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.attachments.Attachment;
import ai.leadplus.domain.attachments.AttachmentRepository;
import ai.leadplus.domain.attachments.SourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final AwsS3Service awsS3Service;

    public AttachmentDto createAttachment(Long sourceId, SourceType sourceType, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        String fileName = file.getOriginalFilename();
        String baseName = fileName != null ? fileName.replaceFirst("\\.[^.]+$", "") : "";
        String fileType = FileReader.getFileExtension(fileName);

        Attachment attachment = Attachment.builder()
                .sourceId(sourceId)
                .sourceType(sourceType)
                .fileName(baseName)
                .fileType(fileType)
                .sizeBytes(file.getSize())
                .active(true)
                .build();
        attachmentRepository.save(attachment);
        String resourceUrl = awsS3Service.uploadFile(file, createFolderName(sourceType), String.valueOf(attachment.getId()));
        attachment.setFileUrl(resourceUrl);
        return AttachmentDto.fromEntity(attachmentRepository.save(attachment));
    }

    public List<AttachmentDto> getAttachments(Long sourceId, SourceType sourceType) {
        List<Attachment> attachments = attachmentRepository.findAllBySourceIdAndSourceTypeAndActiveTrue(sourceId, sourceType);
        return attachments.stream()
                .map(AttachmentDto::fromEntity)
                .toList();
    }

    public Map<Long, List<AttachmentDto>> getAttachmentsBySourceIdsAndSourceType(List<Long> sourceIds, SourceType sourceType) {
        List<Attachment> attachments = attachmentRepository.findAllBySourceIdInAndSourceTypeAndActiveTrue(sourceIds, sourceType);
        return attachments.stream()
                .map(AttachmentDto::fromEntity)
                .collect(Collectors.groupingBy(AttachmentDto::getSourceId));
    }

    public void deleteAttachmentsBySourceIdAndSourceType(Long sourceId, SourceType sourceType) {
        List<Attachment> attachments = attachmentRepository.findAllBySourceIdAndSourceTypeAndActiveTrue(sourceId, sourceType);
        for (Attachment attachment : attachments) {
            deleteAttachment(attachment);
        }
    }

    private void deleteAttachment(Attachment attachment) {
        awsS3Service.deleteExistingFile(attachment.getFileUrl());
        attachment.setActive(false);
        attachmentRepository.save(attachment);
    }

    public void deleteAttachment(String id, SourceType sourceType) {
        Attachment attachment = findAttachmentById(id, sourceType);
        awsS3Service.deleteExistingFile(attachment.getFileUrl());
        attachment.setActive(false);
        attachmentRepository.save(attachment);
    }

    private Attachment findAttachmentById(String id, SourceType sourceType) {
        return attachmentRepository.findByIdAndSourceTypeAndActiveTrue(Long.parseLong(id), sourceType)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + id));
    }

    private String createFolderName(SourceType sourceType) {
        StringBuilder  stringBuilder = new StringBuilder("attachments");
        switch (sourceType) {
            case REQUEST_FOR_QUOTE -> stringBuilder.append("/request-for-quotes");
            case REQUEST_FOR_PROPOSAL -> stringBuilder.append("/request-for-proposals");
            case SHOWCASE -> stringBuilder.append("/showcases");
            case QUOTATION -> stringBuilder.append("/quotations");
        }
        return stringBuilder.toString();
    }
}
