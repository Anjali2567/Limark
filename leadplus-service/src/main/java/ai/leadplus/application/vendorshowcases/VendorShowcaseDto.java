package ai.leadplus.application.vendorshowcases;

import ai.leadplus.domain.vendorshowcases.VendorShowcase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VendorShowcaseDto {

    private Long id;
    private Long vendorId;
    private Long tenantId;
    private String projectName;
    private String clientName;
    private String description;
    private List<Long> serviceIds;
    private String duration;
    private String resultsAndOutcomes;
    private boolean active;
    private LocalDateTime createdAt;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static VendorShowcaseDto fromEntity(VendorShowcase vendorShowcase) {
        return baseBuilder(VendorShowcaseDto.builder(), vendorShowcase)
                .build();
    }

    @SuppressWarnings("unchecked")
    protected static <T extends VendorShowcaseDtoBuilder<?,?>> T baseBuilder(T builder, VendorShowcase vendorShowcase) {
        if (vendorShowcase == null) {
            return null;
        }
        return (T) builder
                .id(vendorShowcase.getId())
                .vendorId(vendorShowcase.getVendorId())
                .tenantId(vendorShowcase.getTenantId())
                .projectName(vendorShowcase.getProjectName())
                .clientName(vendorShowcase.getClientName())
                .description(vendorShowcase.getDescription())
                .serviceIds(vendorShowcase.getServiceIds())
                .duration(vendorShowcase.getDuration())
                .resultsAndOutcomes(vendorShowcase.getResultsAndOutcomes())
                .active(vendorShowcase.isActive())
                .createdAt(vendorShowcase.getCreatedAt())
                .createdBy(vendorShowcase.getCreatedBy())
                .updatedBy(vendorShowcase.getUpdatedBy())
                .updatedAt(vendorShowcase.getUpdatedAt());
    }

    public VendorShowcase toEntity() {
        return VendorShowcase.builder()
                .id(id)
                .vendorId(vendorId)
                .tenantId(tenantId)
                .projectName(projectName)
                .clientName(clientName)
                .description(description)
                .serviceIds(serviceIds)
                .duration(duration)
                .resultsAndOutcomes(resultsAndOutcomes)
                .active(active)
                .createdAt(createdAt)
                .createdBy(createdBy)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
