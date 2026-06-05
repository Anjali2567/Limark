package ai.leadplus.api.v1.users;

import ai.leadplus.application.users.UserDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSignUpRequest {

    private String name;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;

    private String phoneNumber;
    private String company;

    @NotBlank(message = "Password is mandatory")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$",
            message = "Password must be at least 8 characters with uppercase, lowercase, number & special char.")
    private String password;

    public UserDto toDto() {
        return UserDto.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .company(company)
                .email(email)
                .password(password)
                .build();
    }
}
