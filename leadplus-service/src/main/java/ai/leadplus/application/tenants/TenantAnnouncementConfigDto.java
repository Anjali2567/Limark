package ai.leadplus.application.tenants;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantAnnouncementConfigDto {
    private String fromEmail;
    private String senderName;
    private String smtpAppPassword;
}
