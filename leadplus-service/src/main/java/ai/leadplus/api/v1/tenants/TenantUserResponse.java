package ai.leadplus.api.v1.tenants;

import ai.leadplus.application.tenants.TenantUserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantUserResponse {
    private Long id;
    private String name;
    private String email;
    private long workspacesCount;
    private LocalDateTime createdAt;

    public static TenantUserResponse fromDto(TenantUserDto tenantUserDto) {
        return TenantUserResponse.builder()
                .id(tenantUserDto.getId())
                .name(tenantUserDto.getName())
                .email(tenantUserDto.getEmail())
                .workspacesCount(tenantUserDto.getWorkspacesCount())
                .createdAt(tenantUserDto.getCreatedAt())
                .build();
    }
}
