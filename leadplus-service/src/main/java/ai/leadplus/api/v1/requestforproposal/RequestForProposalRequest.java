package ai.leadplus.api.v1.requestforproposal;

import ai.leadplus.application.requestforproposal.RequestForProposalDto;
import ai.leadplus.domain.common.BudgetRange;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestForProposalRequest {

    @NotBlank
    private String title;
    @NotEmpty
    private List<Long> serviceIds;
    private int quantity;
    private BudgetRange budget;
    private String timeline;
    private String description;
    @NotEmpty
    private List<Long> specificationIds;

    public RequestForProposalDto toDto(Long userId) {
        return RequestForProposalDto.builder()
                .userId(userId)
                .title(title)
                .serviceIds(serviceIds)
                .quantity(quantity)
                .budget(budget)
                .timeline(timeline)
                .description(description)
                .specificationIds(specificationIds)
                .build();
    }

    public RequestForProposalDto toDto() {
        return RequestForProposalDto.builder()
                .title(title)
                .serviceIds(serviceIds)
                .quantity(quantity)
                .budget(budget)
                .timeline(timeline)
                .description(description)
                .specificationIds(specificationIds)
                .build();
    }
}
