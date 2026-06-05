package ai.leadplus.api.v1.quotations.requestforquotes;

import ai.leadplus.api.common.TenantValidator;
import ai.leadplus.api.common.VendorValidator;
import ai.leadplus.api.v1.quotations.QuotationRequest;
import ai.leadplus.api.v1.quotations.QuotationResponse;
import ai.leadplus.application.quotations.QuotationDto;
import ai.leadplus.application.quotations.QuotationService;
import ai.leadplus.application.requestforquotes.RequestForQuoteDetailedDto;
import ai.leadplus.application.requestforquotes.RequestForQuoteService;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.application.vendors.VendorDto;
import ai.leadplus.domain.common.SourcingRequestType;
import ai.leadplus.domain.quotations.QuotationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/tenants/{tenantId}/workspaces/{workspaceId}/quotations/request-for-quotes")
@RequiredArgsConstructor
@Tag(name = "Quotation APIs", description = "APIs for managing Quotations")
public class QuotationRequestForQuoteController {

    private final QuotationService quotationService;
    private final VendorValidator vendorValidator;
    private final TenantValidator tenantValidator;
    private final RequestForQuoteService requestForQuoteService;
    private final UserService userService;

    @Operation(summary = "Create New Quotation")
    @PostMapping
    public ResponseEntity<QuotationResponse> createQuotation(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @Valid @RequestBody QuotationRequest request) {
        log.info("[POST] Received request to create Quotation");
        VendorDto vendorDto = vendorValidator.getAuthenticatedVendor();
        tenantValidator.validateWorkspaceByTenantId(tenantId, workspaceId);
        QuotationDto dto = quotationService.createQuotation(request.toDto(tenantId, workspaceId, vendorDto.getId()), SourcingRequestType.REQUEST_FOR_QUOTE);
        QuotationResponse response = QuotationResponse.fromDto(dto);
        log.info("[POST] Created Quotation with ID: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get Quotation by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerQuotationResponse> getQuotationById(@PathVariable String id) {
        log.info("[GET] Received request to get Quotation by ID: {}", id);
        QuotationDto quotationDto = quotationService.getQuotationByIdAndSourceType(id, SourcingRequestType.REQUEST_FOR_QUOTE);
        CustomerQuotationResponse response = mapToCustomerQuotationResponse(quotationDto);
        log.info("[GET] Retrieved Quotation by ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Quotations by Vendor")
    @GetMapping("/vendors")
    public ResponseEntity<Page<CustomerQuotationResponse>> getQuotationByVendorId(Pageable pageable) {
        log.info("[GET] Received request to get Quotations for vendor");
        VendorDto vendorDto = vendorValidator.getAuthenticatedVendor();
        Page<QuotationDto> quotationDtos = quotationService.getAllByVendorIdAndSourceType(vendorDto.getId(), SourcingRequestType.REQUEST_FOR_QUOTE, pageable);
        Page<CustomerQuotationResponse> responses = quotationDtos.map(this::mapToCustomerQuotationResponse);
        log.info("[GET] Returning {} Quotations for vendorId: {}", responses.getTotalElements(), vendorDto.getId());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Update Quotation By Id")
    @PutMapping("/{id}")
    public ResponseEntity<QuotationResponse> updateQuotation(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @PathVariable Long id,
            @Valid @RequestBody QuotationRequest request) {
        log.info("[PUT] Received request to Update Quotation");
        VendorDto vendorDto = vendorValidator.getAuthenticatedVendor();
        tenantValidator.validateWorkspaceByTenantId(tenantId, workspaceId);
        QuotationDto dto = quotationService.updateQuotation(id, request.toDto(tenantId, workspaceId, vendorDto.getId()), SourcingRequestType.REQUEST_FOR_QUOTE);
        QuotationResponse response = QuotationResponse.fromDto(dto);
        log.info("[PUT] Update Quotation with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Quotations by Source")
    @GetMapping("/sources")
    public ResponseEntity<List<CustomerQuotationResponse>> getQuotationBySource(
            @RequestParam Long sourceId) {
        log.info("[GET] Received request to get Quotations for sourceId={}, sourceType={}",
                sourceId, SourcingRequestType.REQUEST_FOR_QUOTE);
        List<QuotationDto> quotationDtos = quotationService
                .getAllBySource(sourceId, SourcingRequestType.REQUEST_FOR_QUOTE);
        List<CustomerQuotationResponse> responses = quotationDtos.stream().map(this::mapToCustomerQuotationResponse).toList();
        log.info("[GET] Returning {} Quotations for sourceId={}", responses.size(), sourceId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Update Quotation By Id")
    @PutMapping("/{id}/accepted")
    public ResponseEntity<QuotationResponse> updateAcceptedQuotationStatus(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @PathVariable Long id) {
        log.info("[PUT] Received request to Update Quotation Status with Accepted");
        VendorDto vendorDto = vendorValidator.getAuthenticatedVendor();
        tenantValidator.validateWorkspaceByTenantId(tenantId, workspaceId);
        QuotationDto dto = quotationService.updateQuotationStatus(id, vendorDto.getId(), QuotationStatus.ACCEPTED, SourcingRequestType.REQUEST_FOR_QUOTE);
        QuotationResponse response = QuotationResponse.fromDto(dto);
        log.info("[PUT] Update Quotation Status with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Quotation By Id")
    @PutMapping("/{id}/rejected")
    public ResponseEntity<QuotationResponse> updateRejectedQuotationStatus(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @PathVariable Long id) {
        log.info("[PUT] Received request to Update Quotation Status with rejected");
        VendorDto vendorDto = vendorValidator.getAuthenticatedVendor();
        tenantValidator.validateWorkspaceByTenantId(tenantId, workspaceId);
        QuotationDto dto = quotationService.updateQuotationStatus(id, vendorDto.getId(), QuotationStatus.REJECTED, SourcingRequestType.REQUEST_FOR_QUOTE);
        QuotationResponse response = QuotationResponse.fromDto(dto);
        log.info("[PUT] Update Quotation Status with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Quotation by ID (Soft Delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuotation(@PathVariable Long id) {
        log.info("[DELETE] Received request to delete Quotation by ID: {}", id);
        VendorDto vendorDto = vendorValidator.getAuthenticatedVendor();
        quotationService.deleteQuotation(id, vendorDto.getId(), SourcingRequestType.REQUEST_FOR_QUOTE);
        log.info("[DELETE] Deleted Quotation with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    private CustomerQuotationResponse mapToCustomerQuotationResponse(QuotationDto quotationDto) {

        Long rfqId = quotationDto.getSourceId();

        RequestForQuoteDetailedDto rfqDto =
                requestForQuoteService.getRequestForQuoteById(rfqId);

        Long userId = rfqDto.getUserId();

        UserDto userDto = userService.getUserById(userId);

        String customerName = userDto.getName();
        String customerCompanyName = userDto.getCompany();

        return CustomerQuotationResponse.fromDto(
                quotationDto,
                customerName,
                customerCompanyName,
                userDto.getEmail(),
                userDto.getPhoneNumber()
        );
    }
}
