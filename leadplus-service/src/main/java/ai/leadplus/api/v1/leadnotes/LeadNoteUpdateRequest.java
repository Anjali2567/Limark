package ai.leadplus.api.v1.leadnotes;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadNoteUpdateRequest {
    @NotNull(message = "Note is required")
    private String note;
}
