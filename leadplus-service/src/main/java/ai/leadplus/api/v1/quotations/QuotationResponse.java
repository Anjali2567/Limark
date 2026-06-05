package ai.leadplus.api.v1.quotations;

import ai.leadplus.application.quotations.QuotationDto;
import ai.leadplus.application.quotations.QuotationItemDto;
import ai.leadplus.domain.common.BudgetRange;
import ai.leadplus.domain.common.SourcingRequestType;
import ai.leadplus.domain.quotations.QuotationStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationResponse {

    private Long id;
    private Long sourceId;
    private Long tenantId;
    private Long workspaceId;
    private String title;
    private String description;
    private BudgetRange budget;
    private LocalDateTime deadline;
    private List<QuotationItemDto> items;
    private String paymentTerms;
    private List<String> deliverables;
    private QuotationStatus status;
    private SourcingRequestType sourceType;
    private Long vendorId;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static QuotationResponse fromDto(QuotationDto dto) {
        if (dto == null) return null;

        return QuotationResponse.builder()
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
                .build();
    }
}
