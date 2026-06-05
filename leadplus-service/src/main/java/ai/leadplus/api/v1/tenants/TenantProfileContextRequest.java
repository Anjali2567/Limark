package ai.leadplus.api.v1.tenants;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantProfileContextRequest {
    @Size(max = 5000, message = "Profile context must be at most 5000 characters.")
    private String profileContext;
}
