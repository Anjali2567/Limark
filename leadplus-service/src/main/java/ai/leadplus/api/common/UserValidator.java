package ai.leadplus.api.common;

import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserValidator {

    private final UserService userService;

    public String getAuthenticatedUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public UserDto getAuthenticatedUser() {
        return userService.getUserById(Long.parseLong(getAuthenticatedUserId()));
    }
}
