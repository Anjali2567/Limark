package ai.leadplus.application.collaborators;

import ai.leadplus.application.users.UserDto;
import ai.leadplus.domain.collaborators.Collaborator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CollaboratorUserDto extends CollaboratorDto {

    private String name;
    private String email;

    public static CollaboratorUserDto toDto(Collaborator collaborator, UserDto userDto) {
        return baseBuilder(CollaboratorUserDto.builder(), collaborator)
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }
}
