package ai.leadplus.application.leadnotes;

import ai.leadplus.domain.common.LeadType;
import ai.leadplus.domain.leadnotes.LeadNote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadNoteDto {

    private Long id;
    private Long tenantId;
    private Long workspaceId;
    private String note;
    private LeadType type;
    private Long sourceId;
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static LeadNoteDto fromEntity(LeadNote entity) {
        return LeadNoteDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .workspaceId(entity.getWorkspaceId())
                .note(entity.getNote())
                .type(entity.getType())
                .sourceId(entity.getSourceId())
                .active(entity.isActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public LeadNote toEntity() {
        return LeadNote.builder()
                .id(id)
                .tenantId(tenantId)
                .workspaceId(workspaceId)
                .note(note)
                .type(type)
                .sourceId(sourceId)
                .active(active)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
