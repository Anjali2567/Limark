package ai.leadplus.application.tenants;

import ai.leadplus.domain.tenants.Module;
import ai.leadplus.domain.tenants.Tenant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantListingDto {
    private Long id;
    private String name;
    private String domain;
    private List<Module> modules;
    private String ownerName;
    private LocalDateTime createdAt;

    public static TenantListingDto fromEntity(Tenant tenant, String ownerName) {
        return TenantListingDto.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .domain(tenant.getDomain())
                .modules(tenant.getModules())
                .ownerName(ownerName)
                .createdAt(tenant.getCreatedAt())
                .build();
    }
}
