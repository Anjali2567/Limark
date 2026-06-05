package ai.leadplus.api.configurations;

import ai.leadplus.application.auth.JwtService;
import ai.leadplus.application.refreshtoken.RefreshTokenService;
import ai.leadplus.application.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration implements WebMvcConfigurer {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain apiKeySecurityFilterChain(HttpSecurity http, AuthenticationProperties props) throws Exception {

        ApiKeyAuthenticationFilter apiKeyFilter = new ApiKeyAuthenticationFilter(props.getApikey());

        JwtAuthorizationFilter jwtFilter =
                new JwtAuthorizationFilter(
                        authenticationManager(http.getSharedObject(AuthenticationConfiguration.class)),
                        jwtService,
                        userDetailsService
                );

        http
                .securityMatcher("/v1/facts",
                        "/v1/facts/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/facts",
                                "/v1/facts/**")
                        .access((authentication, context) -> {
                            if (authentication == null) {
                                return new AuthorizationDecision(false);
                            }
                            if (authentication.get() instanceof ApiKeyAuthentication) {
                                return new AuthorizationDecision(true);
                            }
                            boolean hasAdminRole = authentication.get().getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                            return new AuthorizationDecision(hasAdminRole);
                        })
                )
                .sessionManagement(m -> m.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtFilter, ApiKeyAuthenticationFilter.class)
                .cors(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public SecurityFilterChain companyApiSecurityFilterChain(HttpSecurity http, AuthenticationProperties props) throws Exception {

        ApiKeyAuthenticationFilter apiKeyFilter = new ApiKeyAuthenticationFilter(props.getLeadApiKey());

        JwtAuthorizationFilter jwtFilter =
                new JwtAuthorizationFilter(
                        authenticationManager(http.getSharedObject(AuthenticationConfiguration.class)),
                        jwtService,
                        userDetailsService
                );

        http
                .securityMatcher("/v1/companies/**", "/v1/contacts/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/companies/**", "/v1/contacts/**")
                        .access((authentication, context) -> {
                            if (authentication == null) {
                                return new AuthorizationDecision(false);
                            }
                            if (authentication.get() instanceof ApiKeyAuthentication) {
                                return new AuthorizationDecision(true);
                            }
                            boolean hasAdminRole = authentication.get().getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                            return new AuthorizationDecision(hasAdminRole);
                        })
                )
                .sessionManagement(m -> m.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtFilter, ApiKeyAuthenticationFilter.class)
                .cors(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(authenticationManager(http.getSharedObject(AuthenticationConfiguration.class)), jwtService, refreshTokenService, userService);
        jwtAuthenticationFilter.setFilterProcessesUrl("/v1/auth/login");

        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request.requestMatchers(
                                "/",
                                "/v1/auth/**",
                                "/v1/unsubscribe",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error",
                                "/actuator/health",
                                "/oauth2/**",
                                "/v1/workspaces/invitation/**",
                                "/v1/chat",
                                "/v1/tenants/modules",
                                "/v1/specifications",
                                "/v1/services",
                                "/v1/industries",
                                "/v1/vendors/search",
                                "/v1/vendors/search/parse"
                        ).permitAll()
                        .requestMatchers("/v1/prompt-specifications/**", "/v1/admin/**")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .hasAnyRole("CUSTOMER", "VENDOR", "GUEST", "USER", "ADMIN"))
                .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore((new JwtAuthorizationFilter(authenticationManager(http.getSharedObject(AuthenticationConfiguration.class)), jwtService, userDetailsService)), UsernamePasswordAuthenticationFilter.class);
        http.cors(Customizer.withDefaults());
        return http.build();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
