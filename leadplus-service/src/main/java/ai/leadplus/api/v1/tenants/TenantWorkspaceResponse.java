package ai.leadplus.api.v1.tenants;

import ai.leadplus.application.tenants.TenantWorkspaceDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantWorkspaceResponse {
    private Long id;
    private String workspaceName;
    private Long ownerId;
    private String ownerName;
    private long totalMembers;
    private LocalDateTime createdAt;

    public static TenantWorkspaceResponse fromDto(TenantWorkspaceDto tenantWorkspaceDto) {
        return TenantWorkspaceResponse.builder()
                .id(tenantWorkspaceDto.getId())
                .workspaceName(tenantWorkspaceDto.getWorkspaceName())
                .ownerId(tenantWorkspaceDto.getOwnerId())
                .ownerName(tenantWorkspaceDto.getOwnerName())
                .totalMembers(tenantWorkspaceDto.getTotalMembers())
                .createdAt(tenantWorkspaceDto.getCreatedAt())
                .build();
    }
}
