package ai.leadplus.application.tenantcompanies;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantCompanyPayload {
    private String sourceId;
    private String name;
}
