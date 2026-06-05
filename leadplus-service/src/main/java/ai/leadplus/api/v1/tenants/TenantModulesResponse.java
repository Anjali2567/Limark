package ai.leadplus.api.v1.tenants;

import ai.leadplus.domain.tenants.Module;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantModulesResponse {

    public List<Module> modules;
}
