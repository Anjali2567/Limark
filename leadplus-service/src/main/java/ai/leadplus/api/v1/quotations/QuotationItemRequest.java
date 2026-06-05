package ai.leadplus.api.v1.quotations;

import ai.leadplus.application.quotations.QuotationItemDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationItemRequest {

    @NotBlank
    private String serviceName;
    private String description;
    private String duration;
    private BigDecimal price;

    public QuotationItemDto toDto() {
        return QuotationItemDto.builder()
                .serviceName(serviceName)
                .description(description)
                .duration(duration)
                .price(price)
                .build();
    }
}
