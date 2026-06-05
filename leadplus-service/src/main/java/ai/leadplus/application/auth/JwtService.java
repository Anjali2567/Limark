package ai.leadplus.application.auth;

import ai.leadplus.domain.users.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token.expiration-minutes}")
    private long accessTokenExpirationMinutes;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(
            String userId,
            String workspaceId,
            String tenantId,
            String name,
            String email,
            List<UserRole> roles,
            boolean verified,
            boolean active) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("workspaceId", workspaceId);
        claims.put("tenantId", tenantId);
        claims.put("name", name);
        claims.put("email", email);
        claims.put("roles", roles);
        claims.put("verified", verified);
        claims.put("active", active);
        return createToken(claims, userId);
    }

    private String createToken(Map<String, Object> claims, String username) {
        long expirationMillis = accessTokenExpirationMinutes * 60 * 1000;

        return Jwts.builder().claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    public String extractUserIdFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserRoleFromToken(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Boolean extractActiveFromToken(String token) {
        return extractClaim(token, claims -> claims.get("active", Boolean.class));
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUserIdFromToken(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
