package ai.leadplus.api.v1.requestforquotes;

import ai.leadplus.application.requestforquotes.RequestForQuoteDto;
import ai.leadplus.domain.common.BudgetRange;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RequestForQuoteResponse {

    private Long id;
    private String title;
    private List<Long> serviceIds;
    private List<Long> vendorIds;
    private int quantity;
    private BudgetRange budget;
    private String description;
    private LocalDateTime deadline;

    public static RequestForQuoteResponse fromDto(RequestForQuoteDto dto) {
        if (dto == null) {
            return null;
        }
        return baseBuilder(RequestForQuoteResponse.builder(), dto)
                .build();
    }

    @SuppressWarnings("unchecked")
    protected static <T extends RequestForQuoteResponseBuilder<?,?>> T baseBuilder(T builder, RequestForQuoteDto dto) {
        if (dto == null) {
            return null;
        }

        return (T) builder
                .id(dto.getId())
                .title(dto.getTitle())
                .serviceIds(dto.getServiceIds())
                .vendorIds(dto.getVendorIds())
                .quantity(dto.getQuantity())
                .budget(dto.getBudget())
                .description(dto.getDescription())
                .deadline(dto.getDeadline());
    }
}
