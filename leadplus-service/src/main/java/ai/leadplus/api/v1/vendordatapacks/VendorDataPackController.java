package ai.leadplus.api.v1.vendordatapacks;

import ai.leadplus.application.vendordatapacks.VendorDataPackDto;
import ai.leadplus.application.vendordatapacks.VendorDataPackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/v1/tenants/{tenantId}/vendors/{vendorId}/vendor-data-packs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vendor Data Pack")
public class VendorDataPackController {

    private final VendorDataPackService vendorDataPackService;

    @GetMapping("/{id}")
    @Operation(summary = "Get Vendor Data Pack by Id")
    public ResponseEntity<VendorDataPackResponse> getById(@PathVariable String id) {
        log.info("[GET] Received request to get Vendor Data Pack with id: {}", id);
        VendorDataPackDto vendorDataPackDto = vendorDataPackService.getById(id);
        VendorDataPackResponse vendorDataPackResponse = VendorDataPackResponse.fromDto(vendorDataPackDto);
        log.info("[GET] Retrieved Vendor Data Pack with id: {}", vendorDataPackDto.getId());
        return ResponseEntity.ok(vendorDataPackResponse);
    }

    @GetMapping
    @Operation(summary = "Get All Vendor Data Packs")
    public ResponseEntity<List<VendorDataPackResponse>> getAll(
            @PathVariable Long tenantId,
            @PathVariable String vendorId) {
        log.info("[GET] Received request to get Vendor Data Packs for tenant={}, vendor={}", tenantId, vendorId);
        List<VendorDataPackDto> vendorDataPackDtos = vendorDataPackService.getAll(String.valueOf(tenantId), vendorId);
        List<VendorDataPackResponse> response = vendorDataPackDtos
                .stream()
                .map(VendorDataPackResponse::fromDto)
                .toList();
        log.info("[GET] Retrieved Vendor Data Packs size {}", response.size());
        return ResponseEntity.ok(response);
    }
}
