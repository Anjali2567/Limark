package ai.leadplus.api.v1.vendors;

import ai.leadplus.api.common.UserValidator;
import ai.leadplus.application.vendors.VendorDto;
import ai.leadplus.application.vendors.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/v1/users/me/vendor")
@RequiredArgsConstructor
@Tag(name = "Vendor User Management", description = "Endpoints for vendor users to manage their vendor profiles")
public class VendorUserController {

    private final VendorService vendorService;
    private final UserValidator userValidator;

    @Operation(summary = "Become a Vendor")
    @PostMapping
    public ResponseEntity<VendorResponse> becomeVendor() {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[POST] Request received to become Vendor By userId:{}", userId);
        VendorDto vendorDto = vendorService.becomeVendor(Long.parseLong(userId));
        log.info("[POST] Successfully became Vendor By userId:{}", userId);
        return ResponseEntity.ok(VendorResponse.fromDto(vendorDto));
    }

    @Operation(summary = "Update Vendor")
    @PutMapping
    public ResponseEntity<VendorResponse> updateVendor(
            @Valid @RequestBody VendorRequest request) {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[PUT] Request received to update Vendor By userId:{}", userId);
        VendorDto updated = vendorService.updateVendor(Long.parseLong(userId), request.toDto());
        log.info("[PUT] Successfully updated Vendor By userId:{}", userId);
        return ResponseEntity.ok(VendorResponse.fromDto(updated));
    }

    @Operation(summary = "Get Vendor by Id")
    @GetMapping
    public ResponseEntity<VendorResponse> getVendorById() {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[GET] Fetching Vendor By userId={}", userId);
        VendorDto vendor = vendorService.getVendorByUserId(Long.parseLong(userId));
        log.info("[GET] Successfully fetched Vendor By userId={}", userId);
        return ResponseEntity.ok(VendorResponse.fromDto(vendor));
    }

    @Operation(summary = "Upload Vendor Logo for Current User")
    @PutMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VendorResponse> uploadLogoToVendor(
            @Parameter(description = "File to upload", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file
    ) {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[PUT] Received request to upload Image for Logo by userId: {}", userId);
        VendorDto vendorDto = vendorService.uploadVendorLogo(Long.parseLong(userId), file);
        VendorResponse response = VendorResponse.fromDto(vendorDto);
        log.info("[PUT] Uploaded Image for Vendor with id: {}", userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove Vendor Logo for Current User")
    @DeleteMapping("/logo")
    public ResponseEntity<VendorResponse> removeLogoFromVendor() {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[DELETE] Received request to remove Logo by userId: {}", userId);
        VendorDto vendorDto = vendorService.clearVendorLogo(Long.parseLong(userId));
        log.info("[DELETE] Removed Logo for Vendor with id: {}", userId);
        return ResponseEntity.ok(VendorResponse.fromDto(vendorDto));
    }

    @Operation(summary = "Submit vendor for review")
    @PutMapping("/pending")
    public ResponseEntity<VendorResponse> vendorToPending() {
        log.info("[PUT] Received request to move vendor to pending status");
        String userId = userValidator.getAuthenticatedUserId();
        VendorDto vendorDto = vendorService.updateVendorToPending(Long.parseLong(userId));
        VendorResponse vendorResponse = VendorResponse.fromDto(vendorDto);
        log.info("[PUT] Moved vendor with ID: {} to pending status", vendorResponse.getId());
        return ResponseEntity.ok(vendorResponse);
    }
}
