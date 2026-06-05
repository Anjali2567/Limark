package ai.leadplus.api.v1.mailboxes;

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
public class MailboxSmtpRequest {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String appPassword;
}
