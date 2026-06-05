package ai.leadplus.api.v1.common.collaborators;

import ai.leadplus.application.collaborators.CollaboratorUserDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CollaboratorUserResponse extends CollaboratorResponse {

    private String name;
    private String email;

    public static CollaboratorUserResponse toResponse(CollaboratorUserDto dto) {
        return baseBuilder(CollaboratorUserResponse.builder(), dto)
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }
}
