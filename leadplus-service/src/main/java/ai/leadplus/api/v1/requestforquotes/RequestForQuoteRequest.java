package ai.leadplus.api.v1.requestforquotes;

import ai.leadplus.application.requestforquotes.RequestForQuoteDto;
import ai.leadplus.domain.common.BudgetRange;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestForQuoteRequest {

    @NotBlank
    private String title;
    @NotEmpty
    private List<Long> serviceIds;
    @NotEmpty
    private List<Long> vendorIds;
    private int quantity;
    private BudgetRange budget;
    private LocalDateTime deadline;
    private String description;

    public RequestForQuoteDto toDto(Long userId) {
        return RequestForQuoteDto.builder()
                .userId(userId)
                .title(title)
                .serviceIds(serviceIds)
                .vendorIds(vendorIds)
                .quantity(quantity)
                .budget(budget)
                .deadline(deadline)
                .description(description)
                .build();
    }

    public RequestForQuoteDto toDto() {
        return RequestForQuoteDto.builder()
                .title(title)
                .serviceIds(serviceIds)
                .vendorIds(vendorIds)
                .quantity(quantity)
                .budget(budget)
                .deadline(deadline)
                .description(description)
                .build();
    }
}
