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
public class TenantUserDto {
    private Long id;
    private String name;
    private String email;
    private long workspacesCount;
    private LocalDateTime createdAt;
}
