package ai.leadplus.application.tenantcontacts;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantContactPayload {
    private String sourceId;
    private String email;
    private String firstName;
    private String lastName;
    private String companyId;
}
