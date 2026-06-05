package ai.leadplus.application.auth;

import ai.leadplus.domain.users.User;
import ai.leadplus.domain.users.UserRepository;
import ai.leadplus.domain.users.UserRole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User.UserBuilder;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Configuration
public class UserDetailsServiceConfiguration {

    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository) {
        return userId -> {
            User user = userRepository.findById(Long.valueOf(userId))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(String.valueOf(user.getId()));

            String password = (user.getPassword() == null || user.getPassword().isEmpty())
                    ? generateRandomPassword()
                    : user.getPassword();

            List<UserRole> roles = user.getRoles() != null
                    ? user.getRoles()
                    : Collections.emptyList();

            List<GrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                    .collect(Collectors.toList());

            return builder.password(password).authorities(authorities).build();
        };
    }

    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
