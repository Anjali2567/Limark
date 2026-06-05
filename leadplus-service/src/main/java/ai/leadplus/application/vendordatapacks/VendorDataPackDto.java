package ai.leadplus.application.vendordatapacks;

import ai.leadplus.domain.vendordatapacks.VendorDataPack;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDataPackDto {

    private Long id;
    private Long vendorId;
    private Long tenantId;
    private Long leadDataPackId;
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private String assignedBy;

    public static VendorDataPackDto fromEntity(VendorDataPack entity) {
        if (entity == null) return null;

        return VendorDataPackDto.builder()
                .id(entity.getId())
                .vendorId(entity.getVendorId())
                .tenantId(entity.getTenantId())
                .leadDataPackId(entity.getLeadDataPackId())
                .active(entity.isActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .assignedBy(entity.getAssignedBy())
                .build();
    }

    public VendorDataPack toEntity() {
        return VendorDataPack.builder()
                .id(id)
                .vendorId(vendorId)
                .tenantId(tenantId)
                .leadDataPackId(leadDataPackId)
                .active(active)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .assignedBy(assignedBy)
                .build();
    }
}
