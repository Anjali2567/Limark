package ai.leadplus.api.v1.admin.tenant;

import ai.leadplus.application.tenants.TenantAnalyticsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTenantAnalyticsResponse {
    private String tenantName;
    private long totalCampaigns;
    private long activeCampaigns;
    private long emailsSent;
    private long individualEmails;
    private long workspaceUsers;
    private LocalDateTime lastActivity;

    public static AdminTenantAnalyticsResponse fromDto(TenantAnalyticsDto dto) {
        return AdminTenantAnalyticsResponse.builder()
                .tenantName(dto.getTenantName())
                .totalCampaigns(dto.getTotalCampaigns())
                .activeCampaigns(dto.getActiveCampaigns())
                .emailsSent(dto.getEmailsSent())
                .individualEmails(dto.getIndividualEmails())
                .workspaceUsers(dto.getWorkspaceUsers())
                .lastActivity(dto.getLastActivity())
                .build();
    }
}
