package ai.leadplus.api.v1.attachmentlibraries;

import ai.leadplus.application.attachmentlibraries.AttachmentLibraryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentLibraryResponse {
    private String id;
    private Long workspaceId;
    private String filename;
    private String fileUrl;
    private String fileType;
    private long sizeBytes;
    private LocalDateTime updatedAt;
    private Long updatedBy;

    public static AttachmentLibraryResponse fromDto(AttachmentLibraryDto attachmentLibraryDto) {
        return AttachmentLibraryResponse.builder()
                .id(String.valueOf(attachmentLibraryDto.getId()))
                .workspaceId(attachmentLibraryDto.getWorkspaceId())
                .filename(attachmentLibraryDto.getFilename())
                .fileUrl(attachmentLibraryDto.getFileUrl())
                .fileType(attachmentLibraryDto.getFileType())
                .sizeBytes(attachmentLibraryDto.getSizeBytes())
                .updatedAt(attachmentLibraryDto.getUpdatedAt())
                .updatedBy(attachmentLibraryDto.getUpdatedBy())
                .build();
    }
}
