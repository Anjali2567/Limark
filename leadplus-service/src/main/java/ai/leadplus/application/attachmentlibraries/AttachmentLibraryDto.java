package ai.leadplus.application.attachmentlibraries;

import ai.leadplus.domain.attachmentlibraries.AttachmentLibrary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentLibraryDto {
    private Long id;
    private Long workspaceId;
    private String filename;
    private String fileUrl;
    private String fileType;
    private long sizeBytes;
    private LocalDateTime updatedAt;
    private Long updatedBy;

    public static AttachmentLibraryDto fromEntity(AttachmentLibrary attachmentLibrary) {
        return AttachmentLibraryDto.builder()
                .id(attachmentLibrary.getId())
                .workspaceId(attachmentLibrary.getWorkspaceId())
                .filename(attachmentLibrary.getFilename())
                .fileUrl(attachmentLibrary.getFileUrl())
                .fileType(attachmentLibrary.getFileType())
                .sizeBytes(attachmentLibrary.getSizeBytes())
                .updatedAt(attachmentLibrary.getUpdatedAt())
                .updatedBy(attachmentLibrary.getUpdatedBy())
                .build();
    }

    public AttachmentLibrary toEntity() {
        return AttachmentLibrary.builder()
                .id(id)
                .workspaceId(workspaceId)
                .filename(filename)
                .fileUrl(fileUrl)
                .fileType(fileType)
                .sizeBytes(sizeBytes)
                .updatedAt(updatedAt)
                .updatedBy(updatedBy)
                .build();
    }
}
