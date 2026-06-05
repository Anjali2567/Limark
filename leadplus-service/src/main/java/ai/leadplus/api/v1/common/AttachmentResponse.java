package ai.leadplus.api.v1.common;

import ai.leadplus.application.attachments.AttachmentDto;
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
public class AttachmentResponse {

    private Long id;
    private Long sourceId;
    private SourceType sourceType;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private long sizeBytes;
    private Long createdBy;
    private LocalDateTime createdAt;

    public static AttachmentResponse fromDto(AttachmentDto attachmentDto) {
        if (attachmentDto == null)
            return null;
        return AttachmentResponse.builder()
                .id(attachmentDto.getId())
                .sourceId(attachmentDto.getSourceId())
                .sourceType(attachmentDto.getSourceType())
                .fileName(attachmentDto.getFileName())
                .fileType(attachmentDto.getFileType())
                .fileUrl(attachmentDto.getFileUrl())
                .sizeBytes(attachmentDto.getSizeBytes())
                .createdBy(attachmentDto.getCreatedBy())
                .createdAt(attachmentDto.getCreatedAt())
                .build();
    }
}
