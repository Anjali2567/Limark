package ai.leadplus.api.configurations;

import ai.leadplus.application.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Component
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtService jwtService, UserDetailsService userDetailsService) {
        super(authenticationManager);
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader("Authorization");

        if (Objects.isNull(header) || !header.startsWith("Bearer ")) {

            String headerUserId = request.getHeader("X-Guest-Id");

            if (headerUserId == null || headerUserId.isEmpty()) {
                headerUserId = "temp_"+ UUID.randomUUID();
            }
            response.setHeader("X-Guest-Id", headerUserId);
            response.setHeader("Access-Control-Expose-Headers", "X-Guest-Id");

            chain.doFilter(request, response);
            return;
        }

        if (!jwtService.extractActiveFromToken(header.replace("Bearer ", ""))) {
            response.sendError(403,"User Forbidden");
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        UsernamePasswordAuthenticationToken authentication = null;
        if (Objects.nonNull(token)) {
            String userId = jwtService.extractUserIdFromToken(token.replace("Bearer ", ""));
            String userRole = jwtService.extractUserRoleFromToken(token.replace("Bearer ", ""));
            if (Objects.nonNull(userId) || Objects.nonNull(userRole)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
                if (jwtService.validateToken(token.replace("Bearer ", ""), userDetails)) {
                    authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                }
            }
        }
        return authentication;
    }
}