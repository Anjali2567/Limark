package ai.leadplus.api.v1.leadnotes;

import ai.leadplus.api.v1.common.UserIdentity;
import ai.leadplus.application.leadnotes.LeadNoteDto;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.domain.common.LeadType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LeadNoteResponse {

    private Long id;
    private Long tenantId;
    private Long workspaceId;
    private String note;
    private LeadType type;
    private Long sourceId;
    private boolean active;
    private UserIdentity createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static LeadNoteResponse fromDto(LeadNoteDto dto) {
        return LeadNoteResponse.builder()
                .id(dto.getId())
                .tenantId(dto.getTenantId())
                .workspaceId(dto.getWorkspaceId())
                .note(dto.getNote())
                .type(dto.getType())
                .sourceId(dto.getSourceId())
                .active(dto.isActive())
                .createdAt(dto.getCreatedAt())
                .updatedBy(dto.getUpdatedBy())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public static LeadNoteResponse fromDto(LeadNoteDto leadNoteDto, UserDto userDto) {
        return LeadNoteResponse.builder()
                .id(leadNoteDto.getId())
                .tenantId(leadNoteDto.getTenantId())
                .workspaceId(leadNoteDto.getWorkspaceId())
                .note(leadNoteDto.getNote())
                .type(leadNoteDto.getType())
                .sourceId(leadNoteDto.getSourceId())
                .active(leadNoteDto.isActive())
                .createdAt(leadNoteDto.getCreatedAt())
                .createdBy(userDto != null ? UserIdentity.fromEntity(userDto) : null)
                .updatedAt(leadNoteDto.getUpdatedAt())
                .updatedBy(leadNoteDto.getUpdatedBy())
                .build();
    }
}
