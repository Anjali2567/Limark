package ai.leadplus.application.refreshtoken;

import ai.leadplus.domain.refreshtoken.RefreshToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenDto {

    private Long id;
    private Long userId;
    private String token;
    private int refreshCount;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime lastUsedAt;
    private boolean revoked;

    public static RefreshTokenDto fromEntity(RefreshToken refreshToken) {
        return RefreshTokenDto.builder()
                .id(refreshToken.getId())
                .userId(refreshToken.getUserId())
                .token(refreshToken.getToken())
                .refreshCount(refreshToken.getRefreshCount())
                .issuedAt(refreshToken.getIssuedAt())
                .expiresAt(refreshToken.getExpiresAt())
                .lastUsedAt(refreshToken.getLastUsedAt())
                .revoked(refreshToken.isRevoked())
                .build();
    }

    public RefreshToken toEntity() {
        return RefreshToken.builder()
                .id(id)
                .userId(userId)
                .token(token)
                .refreshCount(refreshCount)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .lastUsedAt(lastUsedAt)
                .revoked(revoked)
                .build();
    }
}
