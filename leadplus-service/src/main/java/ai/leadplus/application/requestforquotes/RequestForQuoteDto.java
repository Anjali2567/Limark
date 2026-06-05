package ai.leadplus.application.requestforquotes;

import ai.leadplus.domain.common.BudgetRange;
import ai.leadplus.domain.common.RequestStatus;
import ai.leadplus.domain.requestforquotes.RequestForQuote;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RequestForQuoteDto {

    private Long id;
    private Long userId;
    private String title;
    private List<Long> serviceIds;
    private List<Long> vendorIds;
    private int quantity;
    private BudgetRange budget;
    private LocalDateTime deadline;
    private String description;
    private RequestStatus status;
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static RequestForQuoteDto fromEntity(RequestForQuote entity) {
        if (entity == null) {
            return null;
        }
        return baseBuilder(RequestForQuoteDto.builder(), entity)
                .build();
    }

    @SuppressWarnings("unchecked")
    protected static <T extends RequestForQuoteDtoBuilder<?, ?>> T baseBuilder(T builder, RequestForQuote entity) {
        if (entity == null) {
            return null;
        }
        return (T) builder
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .serviceIds(entity.getServiceIds())
                .vendorIds(entity.getVendorIds())
                .quantity(entity.getQuantity())
                .budget(entity.getBudget())
                .deadline(entity.getDeadline())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .active(entity.isActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt());
    }

    public RequestForQuote toEntity() {
        return RequestForQuote.builder()
                .id(id)
                .userId(userId)
                .title(title)
                .serviceIds(serviceIds)
                .vendorIds(vendorIds)
                .quantity(quantity)
                .budget(budget)
                .deadline(deadline)
                .description(description)
                .status(status)
                .active(active)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
