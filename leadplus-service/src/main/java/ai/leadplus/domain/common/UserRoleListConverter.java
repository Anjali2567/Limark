package ai.leadplus.domain.common;

import ai.leadplus.domain.users.UserRole;
import jakarta.persistence.Converter;

@Converter
public class UserRoleListConverter extends EnumListConverter<UserRole> {

    public UserRoleListConverter() {
        super(UserRole.class);
    }
}
