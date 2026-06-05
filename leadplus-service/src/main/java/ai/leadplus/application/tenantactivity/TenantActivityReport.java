package ai.leadplus.application.tenantactivity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantActivityReport {
    private Long tenantId;
    private long sessionCount;
    private double activeHours;
    private double engagementRate;
    private long engagedSessionCount;
    private String engagementLabel;
}
