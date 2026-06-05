package ai.leadplus.application.requestforproposal;

import ai.leadplus.domain.common.BudgetRange;
import ai.leadplus.domain.common.RequestStatus;
import ai.leadplus.domain.requestforproposal.RequestForProposal;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RequestForProposalDto {

    private Long id;
    private Long userId;
    private String title;
    private List<Long> serviceIds;
    private int quantity;
    private BudgetRange budget;
    private String timeline;
    private String description;
    private RequestStatus status;
    private List<Long> specificationIds;
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static RequestForProposalDto fromEntity(RequestForProposal entity) {
        if (entity == null) {
            return null;
        }
        return baseBuilder(RequestForProposalDto.builder(), entity)
                .build();
    }

    @SuppressWarnings("unchecked")
    protected static <T extends RequestForProposalDtoBuilder<?, ?>> T baseBuilder(T builder, RequestForProposal entity) {
        if (entity == null) {
            return null;
        }
        return (T) builder
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .serviceIds(entity.getServiceIds())
                .quantity(entity.getQuantity())
                .budget(entity.getBudget())
                .timeline(entity.getTimeline())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .specificationIds(entity.getSpecificationIds())
                .active(entity.isActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt());
    }

    public RequestForProposal toEntity() {
        return RequestForProposal.builder()
                .id(id)
                .userId(userId)
                .title(title)
                .serviceIds(serviceIds)
                .quantity(quantity)
                .budget(budget)
                .timeline(timeline)
                .description(description)
                .status(status)
                .specificationIds(specificationIds)
                .active(active)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
