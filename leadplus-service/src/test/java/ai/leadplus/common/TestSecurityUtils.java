package ai.leadplus.common;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public final class TestSecurityUtils {

    private TestSecurityUtils() {}

    public static void mockAuthenticatedUser(String userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(() -> "ROLE_USER")
                )
        );
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}

