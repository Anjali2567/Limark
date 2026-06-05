package ai.leadplus.api.v1.vendorshowcases;

import ai.leadplus.application.vendorshowcases.VendorShowcaseDto;
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
public class VendorShowcaseResponse {

    private Long id;
    private Long vendorId;
    private Long tenantId;
    private String projectName;
    private String clientName;
    private String description;
    private List<Long> serviceIds;
    private String duration;
    private String resultsAndOutcomes;
    private LocalDateTime createdAt;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static VendorShowcaseResponse fromDto(VendorShowcaseDto vendorShowcaseDto) {
        return baseBuilder(VendorShowcaseResponse.builder(), vendorShowcaseDto)
                .build();
    }

    @SuppressWarnings("unchecked")
    protected static <T extends VendorShowcaseResponseBuilder<?,?>> T baseBuilder(T builder, VendorShowcaseDto vendorShowcaseDto) {
        if (vendorShowcaseDto == null) {
            return null;
        }
        return (T) builder
                .id(vendorShowcaseDto.getId())
                .vendorId(vendorShowcaseDto.getVendorId())
                .tenantId(vendorShowcaseDto.getTenantId())
                .projectName(vendorShowcaseDto.getProjectName())
                .clientName(vendorShowcaseDto.getClientName())
                .description(vendorShowcaseDto.getDescription())
                .serviceIds(vendorShowcaseDto.getServiceIds())
                .duration(vendorShowcaseDto.getDuration())
                .resultsAndOutcomes(vendorShowcaseDto.getResultsAndOutcomes())
                .createdAt(vendorShowcaseDto.getCreatedAt())
                .createdBy(vendorShowcaseDto.getCreatedBy())
                .updatedBy(vendorShowcaseDto.getUpdatedBy())
                .updatedAt(vendorShowcaseDto.getUpdatedAt());
    }
}
