package ai.leadplus.application.quotations;

import ai.leadplus.domain.common.BudgetRange;
import ai.leadplus.domain.quotations.Quotation;
import ai.leadplus.domain.quotations.QuotationItem;
import ai.leadplus.domain.common.SourcingRequestType;
import ai.leadplus.domain.quotations.QuotationStatus;
import lombok.*;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationDto {

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
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static QuotationDto fromEntity(Quotation entity) {
        if (entity == null) return null;

        List<QuotationItemDto> quotationItems = CollectionUtils.isEmpty(entity.getItems()) ?
                List.of() :
                entity.getItems().stream()
                        .map(QuotationItemDto::fromEntity)
                        .toList();

        return QuotationDto.builder()
                .id(entity.getId())
                .sourceId(entity.getSourceId())
                .tenantId(entity.getTenantId())
                .workspaceId(entity.getWorkspaceId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .budget(entity.getBudget())
                .deadline(entity.getDeadline())
                .items(quotationItems)
                .paymentTerms(entity.getPaymentTerms())
                .deliverables(entity.getDeliverables())
                .status(entity.getStatus())
                .sourceType(entity.getSourceType())
                .vendorId(entity.getVendorId())
                .active(entity.isActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public Quotation toEntity() {

        List<QuotationItem> quotationItems = CollectionUtils.isEmpty(items) ?
                List.of() :
                items.stream()
                        .map(QuotationItemDto::toEntity)
                        .toList();

        return Quotation.builder()
                .id(id)
                .sourceId(sourceId)
                .tenantId(tenantId)
                .workspaceId(workspaceId)
                .title(title)
                .description(description)
                .budget(budget)
                .deadline(deadline)
                .items(quotationItems)
                .paymentTerms(paymentTerms)
                .deliverables(deliverables)
                .status(status)
                .sourceType(sourceType)
                .vendorId(vendorId)
                .active(active)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
