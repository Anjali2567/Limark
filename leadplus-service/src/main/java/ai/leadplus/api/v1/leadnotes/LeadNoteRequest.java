package ai.leadplus.api.v1.leadnotes;

import ai.leadplus.application.leadnotes.LeadNoteDto;
import ai.leadplus.domain.common.LeadType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadNoteRequest {

    @NotNull(message = "Note is required")
    private String note;

    @NotNull(message = "Lead type is required")
    private LeadType type;

    @NotNull(message = "Source ID is required")
    private Long sourceId;

    public LeadNoteDto toDto() {
        return LeadNoteDto.builder()
                .note(note)
                .type(type)
                .sourceId(sourceId)
                .build();
    }
}
