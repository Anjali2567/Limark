package ai.leadplus.api.configurations;

import ai.leadplus.api.v1.auth.JwtAuthenticationResponse;
import ai.leadplus.api.v1.auth.LoginRequest;
import ai.leadplus.application.auth.JwtService;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.exception.UserNotFoundAuthenticationException;
import ai.leadplus.application.refreshtoken.RefreshTokenDto;
import ai.leadplus.application.refreshtoken.RefreshTokenService;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.ArrayList;

@AllArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private RefreshTokenService refreshTokenService;
    private UserService userService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRequest loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);
            UserDto user = userService.getUserByEmail(loginRequest.getEmail());

            request.setAttribute("rememberMe", loginRequest.isRememberMe());

            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getId(), loginRequest.getPassword(), new ArrayList<>()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse login request", e);
        }  catch (ResourceNotFoundException e) {
            throw new UserNotFoundAuthenticationException(e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        UserDetails userDetails = (UserDetails) authResult.getPrincipal();
        UserDto user = userService.getUserById(Long.parseLong(userDetails.getUsername()));

        boolean rememberMe = Boolean.TRUE.equals(request.getAttribute("rememberMe"));
        String accessToken = jwtService.generateToken(
                String.valueOf(user.getId()),
                user.getWorkspaceId() != null ? String.valueOf(user.getWorkspaceId()) : null,
                user.getTenantId() != null ? String.valueOf(user.getTenantId()) : null,
                user.getName(),
                user.getEmail(),
                user.getRoles(),
                user.isEmailVerified(),
                true);
        RefreshTokenDto refreshTokenDto = refreshTokenService.createRefreshToken(String.valueOf(user.getId()), rememberMe);

        JwtAuthenticationResponse jwtResponse = JwtAuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshTokenDto.getToken()).build();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        new ObjectMapper().writeValue(response.getWriter(), jwtResponse);
    }
}
