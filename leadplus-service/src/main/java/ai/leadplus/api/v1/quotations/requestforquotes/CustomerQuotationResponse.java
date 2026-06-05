package ai.leadplus.api.v1.quotations.requestforquotes;

import ai.leadplus.api.v1.quotations.QuotationResponse;
import ai.leadplus.application.quotations.QuotationDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerQuotationResponse extends QuotationResponse {

    private String customerName;
    private String customerCompanyName;
    private String customerEmail;
    private String customerPhoneNumber;

    public static CustomerQuotationResponse fromDto(QuotationDto dto, String customerName, String customerCompanyName, String customerEmail, String customerPhoneNumber) {
        if (dto == null) return null;

        return CustomerQuotationResponse.builder()
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
                .customerName(customerName)
                .customerCompanyName(customerCompanyName)
                .customerEmail(customerEmail)
                .customerPhoneNumber(customerPhoneNumber)
                .build();
    }
}
