package ai.leadplus.api.v1.admin.vendordatapacks;

import ai.leadplus.api.common.UserValidator;
import ai.leadplus.api.v1.vendordatapacks.VendorDataPackResponse;
import ai.leadplus.application.vendordatapacks.VendorDataPackDto;
import ai.leadplus.application.vendordatapacks.VendorDataPackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/vendors/{vendorId}/data-packs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Vendor Data Pack")
public class AdminVendorDataPackController {

    private final VendorDataPackService vendorDataPackService;
    private final UserValidator userValidator;

    @PostMapping
    @Operation(summary = "Create Vendor Data Pack")
    public ResponseEntity<VendorDataPackResponse> createVendorDataPack(
            @PathVariable String vendorId,
            @Valid @RequestBody AdminVendorDataPackRequest request) {
        log.info("[POST] Received request to create Vendor Data Pack with vendorId: {}", vendorId);
        String userId = userValidator.getAuthenticatedUserId();
        VendorDataPackDto dto = vendorDataPackService.createVendorDataPack(request.toDto(Long.parseLong(vendorId), userId));
        VendorDataPackResponse leadDataPackResponse = VendorDataPackResponse.fromDto(dto);
        log.info("[POST] Created Vendor Data Pack with id: {}", dto.getId());
        return new ResponseEntity<>(leadDataPackResponse, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Vendor Data Pack")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @PathVariable Long vendorId) {
        log.info("[DELETE] request to delete Vendor Data Pack with id: {}", id);
        vendorDataPackService.deleteVendorDataPackById(id, vendorId);
        log.info("[DELETE] Deleted Vendor Data Pack with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
