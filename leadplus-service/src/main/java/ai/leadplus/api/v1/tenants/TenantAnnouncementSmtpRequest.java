package ai.leadplus.api.v1.tenants;

import ai.leadplus.application.tenants.TenantAnnouncementConfigDto;
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
public class TenantAnnouncementSmtpRequest {

    @NotBlank
    @Email
    private String fromEmail;

    @NotBlank
    private String senderName;

    @NotBlank
    private String appPassword;

    public TenantAnnouncementConfigDto toDto() {
        return TenantAnnouncementConfigDto.builder()
                .fromEmail(fromEmail)
                .senderName(senderName)
                .smtpAppPassword(appPassword)
                .build();
    }
}
