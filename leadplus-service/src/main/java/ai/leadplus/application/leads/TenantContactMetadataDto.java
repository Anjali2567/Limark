package ai.leadplus.application.leads;

import ai.leadplus.domain.tenantcontactmetadata.TenantContactMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantContactMetadataDto {
    private String bdName;
    private String bdEmail;
    private String bdPhone;
    private String isrName;
    private String isrEmail;
    private String isrPhone;
    private String priority;
    private String titleCategory;

    public static TenantContactMetadataDto fromEntity(TenantContactMetadata entity) {
        if (entity == null) return null;
        return TenantContactMetadataDto.builder()
                .bdName(entity.getBdName())
                .bdEmail(entity.getBdEmail())
                .bdPhone(entity.getBdPhone())
                .isrName(entity.getIsrName())
                .isrEmail(entity.getIsrEmail())
                .isrPhone(entity.getIsrPhone())
                .priority(entity.getPriority())
                .titleCategory(entity.getTitleCategory())
                .build();
    }
}
