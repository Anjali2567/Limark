package ai.leadplus.api.v1.common;

import ai.leadplus.application.users.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentity {
    private Long id;
    private String name;

    public static UserIdentity fromEntity(UserDto user) {
        return UserIdentity.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}