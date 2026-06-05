package ai.leadplus.api.v1.common.collaborators;

import ai.leadplus.domain.collaborators.CollaboratorRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollaboratorRequest {

    @NotBlank
    private Long userId;

    @NotNull
    private CollaboratorRole role;
}
