package ai.leadplus.api.v1.tenants;

import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.tenants.TenantDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRecipientResponse {
    private Long id;
    private List<RecipientDto> ccRecipients;
    private List<RecipientDto> bccRecipients;

    public static TenantRecipientResponse fromDto(TenantDto tenantDto) {
        return TenantRecipientResponse.builder()
                .id(tenantDto.getId())
                .ccRecipients(tenantDto.getCcRecipients())
                .bccRecipients(tenantDto.getBccRecipients())
                .build();
    }
}
