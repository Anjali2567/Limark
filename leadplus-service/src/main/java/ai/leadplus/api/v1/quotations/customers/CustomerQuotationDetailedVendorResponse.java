package ai.leadplus.api.v1.quotations.customers;

import ai.leadplus.api.v1.quotations.QuotationResponse;
import ai.leadplus.application.quotations.QuotationDto;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.vendors.AddressDto;
import ai.leadplus.application.vendors.VendorDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerQuotationDetailedVendorResponse extends QuotationResponse {
    private String vendorCompanyName;
    private String vendorLogo;
    private String vendorEmail;
    private String vendorPhoneNumber;
    private String vendorWebsite;
    private String vendorCompanySize;
    private AddressDto vendorAddress;

    public static CustomerQuotationDetailedVendorResponse fromDto(QuotationDto dto, VendorDto vendor, UserDto user) {
        if (dto == null) return null;

        return CustomerQuotationDetailedVendorResponse.builder()
                .id(dto.getId())
                .sourceId(dto.getSourceId())
                .tenantId(dto.getTenantId())
                .workspaceId(dto.getWorkspaceId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .budget(dto.getBudget())
                .deadline(dto.getDeadline())
                .items(dto.getItems())
                .paymentTerms(dto.getPaymentTerms())
                .deliverables(dto.getDeliverables())
                .status(dto.getStatus())
                .sourceType(dto.getSourceType())
                .vendorId(dto.getVendorId())
                .createdBy(dto.getCreatedBy())
                .createdAt(dto.getCreatedAt())
                .updatedBy(dto.getUpdatedBy())
                .updatedAt(dto.getUpdatedAt())
                .vendorCompanyName(vendor != null ? vendor.getCompanyName() : null)
                .vendorLogo(vendor != null ? vendor.getLogo() : null)
                .vendorEmail(user != null ? user.getEmail() : null)
                .vendorPhoneNumber(user != null ? user.getPhoneNumber() : null)
                .vendorWebsite(vendor != null ? vendor.getWebsite() : null)
                .vendorCompanySize(vendor != null ? vendor.getCompanySize() : null)
                .vendorAddress(vendor != null ? vendor.getAddress() : null)
                .build();
    }
}
