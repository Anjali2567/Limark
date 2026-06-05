package ai.leadplus.infrastructure.azure.auth;

import lombok.Data;

@Data
public class AzureRefreshTokenResponse {
    private String token_type;
    private String expires_in;
    private String ext_expires_in;
    private String access_token;
    private String refresh_token;
}
