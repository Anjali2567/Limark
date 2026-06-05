package ai.leadplus.api.v1.vendorshowcases;

import ai.leadplus.application.vendorshowcases.VendorShowcaseDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorShowcaseRequest {

    @NotBlank
    private String projectName;
    private String clientName;
    private String description;
    private List<Long> serviceIds;
    private String duration;
    private String resultsAndOutcomes;

    public VendorShowcaseDto toDto(Long tenantId, Long vendorId) {
        return VendorShowcaseDto.builder()
                .vendorId(vendorId)
                .tenantId(tenantId)
                .projectName(projectName)
                .clientName(clientName)
                .description(description)
                .serviceIds(serviceIds)
                .duration(duration)
                .resultsAndOutcomes(resultsAndOutcomes)
                .build();
    }

    public VendorShowcaseDto toDto() {
        return VendorShowcaseDto.builder()
                .projectName(projectName)
                .clientName(clientName)
                .description(description)
                .serviceIds(serviceIds)
                .duration(duration)
                .resultsAndOutcomes(resultsAndOutcomes)
                .build();
    }
}
