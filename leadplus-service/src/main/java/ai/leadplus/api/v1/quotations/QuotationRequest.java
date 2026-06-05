package ai.leadplus.api.v1.quotations;

import ai.leadplus.application.quotations.QuotationDto;
import ai.leadplus.application.quotations.QuotationItemDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationRequest {

    @NotBlank
    private Long sourceId;

    @Valid
    private List<QuotationItemRequest> items;
    private String paymentTerms;
    private List<String> deliverables;

    public QuotationDto toDto(Long tenantId, Long workspaceId, Long vendorId) {
        List<QuotationItemDto> itemDtoList = CollectionUtils.isEmpty(items) ?
                List.of() :
                items.stream()
                        .map(QuotationItemRequest::toDto)
                        .toList();
        return QuotationDto.builder()
                .tenantId(tenantId)
                .workspaceId(workspaceId)
                .sourceId(sourceId)
                .vendorId(vendorId)
                .items(itemDtoList)
                .paymentTerms(paymentTerms)
                .deliverables(deliverables)
                .active(true)
                .build();
    }
}
