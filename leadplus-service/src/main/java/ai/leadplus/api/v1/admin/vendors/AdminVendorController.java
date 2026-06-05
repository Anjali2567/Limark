package ai.leadplus.api.v1.admin.vendors;

import ai.leadplus.api.v1.vendors.VendorResponse;
import ai.leadplus.api.v1.vendorshowcases.VendorShowcaseWithAttachmentsResponse;
import ai.leadplus.application.vendors.VendorDto;
import ai.leadplus.application.vendors.VendorService;
import ai.leadplus.application.vendorshowcases.VendorShowcaseService;
import ai.leadplus.application.vendorshowcases.VendorShowcaseWithAttachmentsDto;
import ai.leadplus.domain.vendors.VendorVerificationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/admin/vendors")
@RequiredArgsConstructor
@Tag(name = "Admin Vendor Management", description = "Endpoints for managing vendors by admin users")
public class AdminVendorController {

    private final VendorService vendorService;
    private final VendorShowcaseService vendorShowcaseService;

    @Operation(summary = "Get all vendors")
    @GetMapping()
    public ResponseEntity<Page<VendorResponse>> getAllVendors(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<VendorVerificationStatus> vendorVerificationStatusList,
            Pageable pageable) {
        log.info("[GET] Received request to get all vendors with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<VendorDto> vendorDtoPage = vendorService.getAllVendors(query, vendorVerificationStatusList, pageable);
        Page<VendorResponse> responsePage = vendorDtoPage.map(VendorResponse::fromDto);
        log.info("[GET] Returning {} vendors", responsePage.getSize());
        return ResponseEntity.ok(responsePage);
    }

    @Operation(summary = "Get Vendor by Id")
    @GetMapping("/{vendorId}")
    public ResponseEntity<VendorResponse> getVendorById(@PathVariable Long vendorId) {
        log.info("[GET] Received request to get vendor with ID: {}", vendorId);
        VendorDto vendorDto = vendorService.getVendorById(vendorId);
        VendorResponse vendorResponse = VendorResponse.fromDto(vendorDto);
        log.info("[GET] Returning vendor with ID: {}", vendorId);
        return ResponseEntity.ok(vendorResponse);
    }

    @Operation(summary = "Approve vendor")
    @PutMapping("/{vendorId}/approve")
    public ResponseEntity<VendorResponse> approveVendor(@PathVariable Long vendorId) {
        log.info("[PUT] Received request to approve vendor with ID: {}", vendorId);
        VendorDto vendorDto = vendorService.updateVendorStatus(vendorId, VendorVerificationStatus.APPROVED);
        VendorResponse vendorResponse = VendorResponse.fromDto(vendorDto);
        log.info("[PUT] Approved vendor with ID: {}", vendorId);
        return ResponseEntity.ok(vendorResponse);
    }

    @Operation(summary = "Reject vendor")
    @PutMapping("/{vendorId}/reject")
    public ResponseEntity<VendorResponse> rejectVendor(@PathVariable Long vendorId, @RequestBody @Valid AdminVendorReviewRequest request) {
        log.info("[PUT] Received request to reject vendor with ID: {}", vendorId);
        VendorDto vendorDto = vendorService.updateVendorStatus(vendorId, VendorVerificationStatus.REJECTED, request.getReviewComment());
        VendorResponse vendorResponse = VendorResponse.fromDto(vendorDto);
        log.info("[PUT] Rejected vendor with ID: {}", vendorId);
        return ResponseEntity.ok(vendorResponse);
    }

    @Operation(summary = "Get Vendor Showcases by Vendor Id")
    @GetMapping("/{vendorId}/showcases")
    public ResponseEntity<List<VendorShowcaseWithAttachmentsResponse>> getVendorShowcasesByVendorId(@PathVariable Long vendorId) {
        log.info("[GET] Received request to get vendor showcases for vendor with ID: {}", vendorId);
        List<VendorShowcaseWithAttachmentsDto> showcases = vendorShowcaseService.getVendorShowcasesByVendorId(vendorId);
        List<VendorShowcaseWithAttachmentsResponse> response = showcases.stream()
                .map(VendorShowcaseWithAttachmentsResponse::fromDto)
                .toList();
        log.info("[GET] Retrieved {} vendor showcases for vendor with ID: {}", response.size(), vendorId);
        return ResponseEntity.ok(response);
    }
}
