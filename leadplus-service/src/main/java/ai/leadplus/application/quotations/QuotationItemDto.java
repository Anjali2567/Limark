package ai.leadplus.application.quotations;

import ai.leadplus.domain.quotations.QuotationItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationItemDto {

    private String serviceName;
    private String description;
    private String duration;
    private BigDecimal price;

    public static QuotationItemDto fromEntity(QuotationItem item) {
        if (item == null) return null;

        return QuotationItemDto.builder()
                .serviceName(item.getServiceName())
                .description(item.getDescription())
                .duration(item.getDuration())
                .price(item.getPrice())
                .build();
    }

    public QuotationItem toEntity() {
        return QuotationItem.builder()
                .serviceName(serviceName)
                .description(description)
                .duration(duration)
                .price(price)
                .build();
    }
}
