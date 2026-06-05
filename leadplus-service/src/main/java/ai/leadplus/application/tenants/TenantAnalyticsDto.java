package ai.leadplus.application.tenants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantAnalyticsDto {
    private String tenantName;
    private long totalCampaigns;
    private long activeCampaigns;
    private long emailsSent;
    private long individualEmails;
    private long workspaceUsers;
    private LocalDateTime lastActivity;
}
