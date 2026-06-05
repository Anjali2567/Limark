package ai.leadplus.api.v1.admin.vendordatapacks;

import ai.leadplus.application.vendordatapacks.VendorDataPackDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminVendorDataPackRequest {

    @NotNull
    private Long leadDataPackId;

    public VendorDataPackDto toDto(Long vendorId, String userId) {
        return VendorDataPackDto.builder()
                .vendorId(vendorId)
                .assignedBy(userId)
                .leadDataPackId(leadDataPackId)
                .active(true)
                .build();
    }
}
