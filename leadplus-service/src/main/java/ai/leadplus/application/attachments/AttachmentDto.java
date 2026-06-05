package ai.leadplus.application.attachments;

import ai.leadplus.domain.attachments.Attachment;
import ai.leadplus.domain.attachments.SourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDto {

    private Long id;
    private Long sourceId;
    private SourceType sourceType;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private long sizeBytes;
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static AttachmentDto fromEntity(Attachment attachment) {
        if (attachment == null)
            return null;
        return AttachmentDto.builder()
                .id(attachment.getId())
                .sourceId(attachment.getSourceId())
                .sourceType(attachment.getSourceType())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileUrl(attachment.getFileUrl())
                .sizeBytes(attachment.getSizeBytes())
                .active(attachment.isActive())
                .createdBy(attachment.getCreatedBy())
                .createdAt(attachment.getCreatedAt())
                .updatedBy(attachment.getUpdatedBy())
                .updatedAt(attachment.getUpdatedAt())
                .build();
    }

    public Attachment toEntity() {
        return Attachment.builder()
                .id(id)
                .sourceId(sourceId)
                .sourceType(sourceType)
                .fileName(fileName)
                .fileType(fileType)
                .fileUrl(fileUrl)
                .sizeBytes(sizeBytes)
                .active(active)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
