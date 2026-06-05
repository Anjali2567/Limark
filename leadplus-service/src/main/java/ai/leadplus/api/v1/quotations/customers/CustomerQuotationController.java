package ai.leadplus.api.v1.quotations.customers;

import ai.leadplus.api.v1.quotations.QuotationResponse;
import ai.leadplus.application.quotations.QuotationDto;
import ai.leadplus.application.quotations.QuotationService;
import ai.leadplus.application.users.UserService;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.vendors.VendorDto;
import ai.leadplus.application.vendors.VendorService;
import ai.leadplus.domain.common.SourcingRequestType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/v1/customers/quotations")
@RequiredArgsConstructor
@Tag(name = "Quotation APIs", description = "APIs for managing Quotations")
public class CustomerQuotationController {

    private final UserService userService;
    private final VendorService vendorService;
    private final QuotationService quotationService;

    @Operation(summary = "Get Quotation by ID")
    @GetMapping("/{id}")
    public ResponseEntity<QuotationResponse> getQuotationById(@PathVariable String id) {

        log.info("[GET] Received request to get Quotation by ID: {}", id);
        QuotationDto quotationDto = quotationService.getQuotationById(id);
        QuotationResponse response = QuotationResponse.fromDto(quotationDto);
        log.info("[GET] Retrieved Quotation by ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Quotations by Request For QuoteId")
    @GetMapping("/request-for-quotes/{requestForQuoteId}")
    public ResponseEntity<Page<CustomerQuotationDetailedVendorResponse>> getQuotationBySourceId(
            @PathVariable Long requestForQuoteId,
            Pageable pageable) {
        log.info("[GET] Received request to get Quotations for requestForQuoteId: {}", requestForQuoteId);
        Page<QuotationDto> quotationDtos =
                quotationService.getAllBySourceIdAndSourceType(
                        requestForQuoteId,
                        SourcingRequestType.REQUEST_FOR_QUOTE,
                        pageable
                );
        Page<CustomerQuotationDetailedVendorResponse> detailedResponses = mapVendorInformation(quotationDtos);
        log.info("[GET] Returning {} Quotations for requestForQuoteId: {}", quotationDtos.getTotalElements(), requestForQuoteId);
        return ResponseEntity.ok(detailedResponses);
    }

    private Page<CustomerQuotationDetailedVendorResponse> mapVendorInformation(Page<QuotationDto> dtos) {
        List<Long> vendorIds = dtos.getContent().stream()
                .map(QuotationDto::getVendorId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<String, VendorDto> vendorMap = vendorService.getVendorsByIds(vendorIds).stream()
                .collect(Collectors.toMap(v -> String.valueOf(v.getId()), v -> v));

        List<Long> userIds = vendorMap.values().stream()
                .map(VendorDto::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<String, UserDto> userMap = userService.getUsersByIds(userIds).stream()
                .collect(Collectors.toMap(u -> String.valueOf(u.getId()), u -> u));

        return dtos.map(dto -> {
            VendorDto vendor = vendorMap.get(String.valueOf(dto.getVendorId()));
            UserDto user = vendor != null ? userMap.get(String.valueOf(vendor.getUserId())) : null;
            return CustomerQuotationDetailedVendorResponse.fromDto(dto, vendor, user);
        });
    }
}
