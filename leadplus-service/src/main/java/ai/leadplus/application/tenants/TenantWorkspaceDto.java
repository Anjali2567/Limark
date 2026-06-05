package ai.leadplus.application.tenants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantWorkspaceDto {
    private Long id;
    private String workspaceName;
    private Long ownerId;
    private String ownerName;
    private long totalMembers;
    private LocalDateTime createdAt;
}
