package ai.leadplus.application.vendors;

import ai.leadplus.application.services.ServiceDto;
import ai.leadplus.application.specifications.SpecificationDto;
import ai.leadplus.domain.vendors.Vendor;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDetailDto extends VendorDto {

    private List<ServiceDto> serviceList;
    private List<SpecificationDto> specificationList;

    public static VendorDetailDto fromEntity(Vendor vendor, List<ServiceDto> services, List<SpecificationDto> specifications) {
        return baseBuilder(VendorDetailDto.builder(), vendor)
                .serviceList(services)
                .specificationList(specifications)
                .build();
    }
}
