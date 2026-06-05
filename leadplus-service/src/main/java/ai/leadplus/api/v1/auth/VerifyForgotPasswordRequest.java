package ai.leadplus.api.v1.auth;

import ai.leadplus.application.auth.ForgotPasswordDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyForgotPasswordRequest {
    @NotBlank(message = "Password is mandatory")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$",
            message = "Password must be at least 8 characters long and contain at least one digit," +
                    " one lowercase letter, one uppercase letter, and one special character.")
    private String password;
    @NotBlank(message = "Token is mandatory")
    private String token;

    public ForgotPasswordDto toDto() {
        return ForgotPasswordDto.builder()
                .password(password)
                .token(token)
                .build();
    }
}
