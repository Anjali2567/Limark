package ai.leadplus.api.v1.contactemails;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactEmailGenerateRequest {

    @NotBlank(message = "Prompt must not be blank")
    private String prompt;
}

