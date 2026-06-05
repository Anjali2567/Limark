package ai.leadplus.api.v1.tenantcontacts;

import ai.leadplus.application.tenantcontacts.TenantContactDto;
import ai.leadplus.domain.common.CRM;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantContactResponse {
    private Long id;
    private Long tenantId;
    private CRM sourceType;
    private String sourceId;
    private String email;
    private String firstName;
    private String lastName;
    private String companyId;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TenantContactResponse fromDto(TenantContactDto entity) {
        if (entity == null) return null;
        return TenantContactResponse.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .sourceType(entity.getSourceType())
                .sourceId(entity.getSourceId())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .companyId(entity.getCompanyId())
                .companyName(entity.getCompanyName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
