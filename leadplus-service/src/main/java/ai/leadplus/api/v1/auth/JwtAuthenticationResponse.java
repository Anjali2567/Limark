package ai.leadplus.api.v1.auth;

import ai.leadplus.application.auth.JwtAuthenticationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthenticationResponse {
    private String accessToken;
    private String refreshToken;

    public static JwtAuthenticationResponse toResponse(JwtAuthenticationDto jwtAuthenticationDto) {
        return JwtAuthenticationResponse.builder()
                .accessToken(jwtAuthenticationDto.getAccessToken())
                .refreshToken(jwtAuthenticationDto.getRefreshToken())
                .build();
    }
}
