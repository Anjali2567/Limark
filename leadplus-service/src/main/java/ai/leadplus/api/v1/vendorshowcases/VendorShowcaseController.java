package ai.leadplus.api.v1.vendorshowcases;

import ai.leadplus.application.vendorshowcases.VendorShowcaseDto;
import ai.leadplus.application.vendorshowcases.VendorShowcaseService;
import ai.leadplus.application.vendorshowcases.VendorShowcaseWithAttachmentsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/tenants/{tenantId}/vendors/{vendorId}/vendor-showcases")
@RequiredArgsConstructor
@Tag(name = "Vendor Showcase", description = "APIs for managing vendor showcases")
public class VendorShowcaseController {

    private final VendorShowcaseService vendorShowcaseService;

    @Operation(summary = "Get Vendor Showcases by Vendor Id")
    @GetMapping
    public ResponseEntity<List<VendorShowcaseWithAttachmentsResponse>> getVendorShowcasesByVendorId(
            @PathVariable Long tenantId,
            @PathVariable Long vendorId) {
        log.info("[GET] Received request to get vendor showcases for tenant={}, vendor={}", tenantId, vendorId);
        List<VendorShowcaseWithAttachmentsDto> showcases = vendorShowcaseService.getVendorShowcasesByVendorId(vendorId);
        List<VendorShowcaseWithAttachmentsResponse> response = showcases.stream()
                .map(VendorShowcaseWithAttachmentsResponse::fromDto)
                .toList();
        log.info("[GET] Retrieved {} vendor showcases for tenant={}, vendor={}", response.size(), tenantId, vendorId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Vendor Showcase by Id")
    @GetMapping("/{showcaseId}")
    public ResponseEntity<VendorShowcaseWithAttachmentsResponse> getVendorShowcaseById(
            @PathVariable Long tenantId,
            @PathVariable Long vendorId,
            @PathVariable Long showcaseId) {
        log.info("[GET] Received request to get vendor showcase with id={} for tenant={}, vendor={}", showcaseId, tenantId, vendorId);
        VendorShowcaseWithAttachmentsDto showcase = vendorShowcaseService.getVendorShowcaseById(showcaseId);
        VendorShowcaseWithAttachmentsResponse response = VendorShowcaseWithAttachmentsResponse.fromDto(showcase);
        log.info("[GET] Retrieved vendor showcase with id={} for tenant={}, vendor={}", showcaseId, tenantId, vendorId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create Vendor Showcase")
    @PostMapping
    public ResponseEntity<VendorShowcaseResponse> createVendorShowcase(
            @PathVariable Long tenantId,
            @PathVariable Long vendorId,
            @RequestBody @Valid VendorShowcaseRequest request) {
        log.info("[POST] Received request to create vendor showcase for tenant={}, vendor={}", tenantId, vendorId);
        VendorShowcaseDto createdShowcase = vendorShowcaseService.createVendorShowcase(request.toDto(tenantId, vendorId));
        VendorShowcaseResponse response = VendorShowcaseResponse.fromDto(createdShowcase);
        log.info("[POST] Created vendor showcase with id={} for tenant={}, vendor={}", response.getId(), tenantId, vendorId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Update Vendor Showcase")
    @PutMapping("/{showcaseId}")
    public ResponseEntity<VendorShowcaseResponse> updateVendorShowcase(
            @PathVariable Long tenantId,
            @PathVariable Long vendorId,
            @PathVariable Long  showcaseId,
            @RequestBody @Valid VendorShowcaseRequest request) {
        log.info("[PUT] Received request to update vendor showcase with id={} for tenant={}, vendor={}", showcaseId, tenantId, vendorId);
        VendorShowcaseWithAttachmentsDto updatedShowcase = vendorShowcaseService.updateShowcase(showcaseId, request.toDto());
        VendorShowcaseResponse response = VendorShowcaseResponse.fromDto(updatedShowcase);
        log.info("[PUT] Updated vendor showcase with id={} for tenant={}, vendor={}", showcaseId, tenantId, vendorId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload Vendor Showcase File")
    @PutMapping(value = "/{showcaseId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VendorShowcaseWithAttachmentsResponse> uploadVendorShowcaseFile(
            @PathVariable Long tenantId,
            @PathVariable Long vendorId,
            @PathVariable Long showcaseId,
            @Parameter(description = "File to upload", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file
    ) {
        log.info("[PUT] Received request to upload file for vendor showcase with id={} for tenant={}, vendor={}", showcaseId, tenantId, vendorId);
        VendorShowcaseWithAttachmentsDto updatedShowcase = vendorShowcaseService.uploadShowcaseFile(showcaseId, file);
        VendorShowcaseWithAttachmentsResponse response = VendorShowcaseWithAttachmentsResponse.fromDto(updatedShowcase);
        log.info("[PUT] Uploaded file for vendor showcase with id={} for tenant={}, vendor={}", showcaseId, tenantId, vendorId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Vendor Showcase File")
    @DeleteMapping("/{showcaseId}/attachments/{attachmentId}")
    public ResponseEntity<VendorShowcaseWithAttachmentsResponse> deleteVendorShowcaseFile(
            @PathVariable Long tenantId,
            @PathVariable Long vendorId,
            @PathVariable Long showcaseId,
            @PathVariable String attachmentId) {
        log.info("[DELETE] Received request to delete file with id={} for vendor showcase with id={} for tenant={}, vendor={}", attachmentId, showcaseId, tenantId, vendorId);
        VendorShowcaseWithAttachmentsDto updatedShowcase = vendorShowcaseService.deleteShowcaseFile(showcaseId, attachmentId);
        VendorShowcaseWithAttachmentsResponse response = VendorShowcaseWithAttachmentsResponse.fromDto(updatedShowcase);
        log.info("[DELETE] Deleted file with id={} for vendor showcase with id={} for tenant={}, vendor={}", attachmentId, showcaseId, tenantId, vendorId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Vendor Showcase")
    @DeleteMapping("/{showcaseId}")
    public ResponseEntity<Void> deleteVendorShowcase(
            @PathVariable Long tenantId,
            @PathVariable Long vendorId,
            @PathVariable Long showcaseId) {
        log.info("[DELETE] Received request to delete vendor showcase with id={} for tenant={}, vendor={}", showcaseId, tenantId, vendorId);
        vendorShowcaseService.deleteVendorShowcase(showcaseId);
        log.info("[DELETE] Deleted vendor showcase with id={} for tenant={}, vendor={}", showcaseId, tenantId, vendorId);
        return ResponseEntity.noContent().build();
    }
}
