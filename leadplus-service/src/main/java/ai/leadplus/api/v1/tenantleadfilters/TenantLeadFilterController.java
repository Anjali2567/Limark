package ai.leadplus.api.v1.tenantleadfilters;

import ai.leadplus.application.tenantleadfilters.TenantLeadFilterDto;
import ai.leadplus.application.tenantleadfilters.TenantLeadFilterService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/tenants/{tenantId}/tenant-lead-filters")
@RequiredArgsConstructor
@Tag(name = "Tenant Lead Filters", description = "Tenant Lead Filter APIs")
public class TenantLeadFilterController {

    private final TenantLeadFilterService tenantLeadFilterService;

    @PostMapping
    public ResponseEntity<TenantLeadFilterResponse> createTenantLeadFilter(
            @PathVariable Long tenantId,
            @RequestBody TenantLeadFilterRequest request) {

        log.info("[POST]  Received request Create TenantLeadFilter for tenant: {}", tenantId);
        TenantLeadFilterDto dto = tenantLeadFilterService.createTenantLeadFilter(request.toDto(tenantId));
        TenantLeadFilterResponse response = TenantLeadFilterResponse.fromDto(dto);
        log.info("[POST]  Created TenantLeadFilter for tenant: {}", tenantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantLeadFilterResponse> getTenantLeadFilterById(@PathVariable Long tenantId,
                                                                            @PathVariable Long id) {

        log.info("[GET] Received request to Get TenantLeadFilter by id: {}", id);
        TenantLeadFilterResponse response = TenantLeadFilterResponse
                .fromDto(tenantLeadFilterService.getTenantLeadFilterById(id, tenantId));
        log.info("[GET] Get TenantLeadFilter by id: {}", id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TenantLeadFilterResponse>> getTenantLeadFilterByTenantId(@PathVariable Long tenantId) {

        log.info("[GET] Received request to Get TenantLeadFilters by tenantId: {}", tenantId);
        List<TenantLeadFilterResponse> responses = tenantLeadFilterService.getTenantLeadFilterByTenantId(tenantId)
                .stream()
                .map(TenantLeadFilterResponse::fromDto)
                .toList();
        log.info("[GET] Get TenantLeadFilters by tenantId: {}", tenantId);

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TenantLeadFilterResponse> updateTenantLeadFilter(
            @PathVariable Long tenantId,
            @PathVariable Long id,
            @RequestBody TenantLeadFilterRequest request) {

        log.info("[PUT] Received request to Update TenantLeadFilter: {}", id);
        TenantLeadFilterDto updated = tenantLeadFilterService.updateTenantLeadFilter(id, request.toDto(tenantId));
        TenantLeadFilterResponse response = TenantLeadFilterResponse.fromDto(updated);
        log.info("[PUT] Updated TenantLeadFilter: {}", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenantLeadFilter(@PathVariable Long tenantId,
                                                       @PathVariable Long id) {

        log.info("[DELETE] Received request to Delete TenantLeadFilter: {}", id);
        tenantLeadFilterService.deleteTenantLeadFilter(id, tenantId);
        log.info("[DELETE] Deleted TenantLeadFilter: {}", id);
        return ResponseEntity.noContent().build();
    }
}
