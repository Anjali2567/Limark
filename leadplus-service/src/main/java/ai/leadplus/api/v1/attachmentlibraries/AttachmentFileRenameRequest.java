package ai.leadplus.api.v1.attachmentlibraries;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttachmentFileRenameRequest {
    @NotBlank
    private String fileName;
}
