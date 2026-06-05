package ai.leadplus.api.v1.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityAuthRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String redirectUri;
}
