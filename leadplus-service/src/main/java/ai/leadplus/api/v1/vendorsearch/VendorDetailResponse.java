package ai.leadplus.api.v1.vendorsearch;

import ai.leadplus.api.v1.services.ServiceResponse;
import ai.leadplus.api.v1.specifications.SpecificationResponse;
import ai.leadplus.api.v1.vendors.VendorResponse;
import ai.leadplus.application.common.utils.MaskUtils;
import ai.leadplus.application.vendors.VendorDetailDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.CollectionUtils;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDetailResponse extends VendorResponse {

    private List<ServiceResponse> serviceList;
    private List<SpecificationResponse> specificationList;

    public static VendorDetailResponse fromDto(VendorDetailDto dto) {
        if (dto == null) {
            return null;
        }

        List<ServiceResponse> services = CollectionUtils.isEmpty(dto.getServiceList()) ?
                List.of() :
                dto.getServiceList().stream()
                        .map(ServiceResponse::fromDto)
                        .toList();
        List<SpecificationResponse> specs = CollectionUtils.isEmpty(dto.getSpecificationList()) ?
                List.of() :
                dto.getSpecificationList().stream()
                        .map(SpecificationResponse::fromDto)
                        .toList();

        return baseBuilder(VendorDetailResponse.builder(), dto)
                .serviceList(services)
                .specificationList(specs)
                .build();
    }

    public static VendorDetailResponse fromDtoToAnonymous(VendorDetailDto dto) {
        if (dto == null) {
            return null;
        }
        VendorDetailResponse vendorDetailResponse = fromDto(dto);
        vendorDetailResponse.setCompanySize(MaskUtils.maskKeepFirstAndLast(dto.getCompanySize()));
        vendorDetailResponse.setPhoneNumber(MaskUtils.maskKeepFirstAndLast(dto.getPhoneNumber()));
        vendorDetailResponse.setFaxNumber(MaskUtils.maskKeepFirstAndLast(dto.getFaxNumber()));
        List<String> maskedCertifications = CollectionUtils.isEmpty(dto.getCertifications()) ?
                List.of() :
                dto.getCertifications().stream()
                        .map(MaskUtils::maskKeepFirstAndLast)
                        .toList();
        vendorDetailResponse.setCertifications(maskedCertifications);
        return vendorDetailResponse;
    }
}
