package ai.leadplus.api.v1.vendordatapacks;

import ai.leadplus.application.vendordatapacks.VendorDataPackDto;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDataPackResponse {

    private Long id;
    private Long vendorId;
    private Long tenantId;
    private Long leadDataPackId;
    private String assignedBy;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static VendorDataPackResponse fromDto(VendorDataPackDto dto) {
        if (dto == null) return null;

        return VendorDataPackResponse.builder()
                .id(dto.getId())
                .vendorId(dto.getVendorId())
                .tenantId(dto.getTenantId())
                .leadDataPackId(dto.getLeadDataPackId())
                .assignedBy(dto.getAssignedBy())
                .createdBy(dto.getCreatedBy())
                .createdAt(dto.getCreatedAt())
                .updatedBy(dto.getUpdatedBy())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
