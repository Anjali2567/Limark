package ai.leadplus.api.v1.users;

import ai.leadplus.application.users.UserDto;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Pattern(regexp = "^(?!\\s*$).+", message = "Name cannot be blank")
    private String name;

    private String phoneNumber;
    private String company;

    public UserDto toDto() {
        return UserDto.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .company(company)
                .build();
    }
}
