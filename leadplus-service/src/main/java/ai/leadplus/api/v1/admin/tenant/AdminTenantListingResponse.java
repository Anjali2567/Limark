package ai.leadplus.api.v1.admin.tenant;

import ai.leadplus.application.tenants.TenantListingDto;
import ai.leadplus.domain.tenants.Module;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTenantListingResponse {
    private Long id;
    private String name;
    private String domain;
    private List<Module> modules;
    private String ownerName;
    private LocalDateTime createdAt;

    public static AdminTenantListingResponse fromDto(TenantListingDto dto) {
        return AdminTenantListingResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .domain(dto.getDomain())
                .modules(dto.getModules())
                .ownerName(dto.getOwnerName())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
