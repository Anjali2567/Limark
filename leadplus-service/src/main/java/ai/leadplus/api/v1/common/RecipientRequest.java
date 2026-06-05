package ai.leadplus.api.v1.common;

import ai.leadplus.application.common.RecipientDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientRequest {

    @NotBlank
    @Email
    private String email;
    private String name;

    public RecipientDto toDto() {
        return RecipientDto.builder()
                .email(email)
                .name(name)
                .build();
    }
}
