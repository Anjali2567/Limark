package ai.leadplus.api.v1.vendorsearch;

import ai.leadplus.application.vendorqueryparser.VendorSearchParseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VendorSearchParseResponse {
    private List<Long> serviceIds;
    private List<Long> industryIds;
    private List<Long> specificationIds;

    public static VendorSearchParseResponse fromDto(VendorSearchParseDto dto) {
        if (dto == null) {
            return VendorSearchParseResponse.builder()
                    .serviceIds(List.of())
                    .industryIds(List.of())
                    .specificationIds(List.of())
                    .build();
        }
        return VendorSearchParseResponse.builder()
                .serviceIds(dto.getServiceIds())
                .industryIds(dto.getIndustryIds())
                .specificationIds(dto.getSpecificationIds())
                .build();
    }
}
