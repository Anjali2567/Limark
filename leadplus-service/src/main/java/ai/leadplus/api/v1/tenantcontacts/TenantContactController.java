package ai.leadplus.api.v1.tenantcontacts;

import ai.leadplus.api.common.TenantValidator;
import ai.leadplus.application.tenantcontacts.TenantContactDto;
import ai.leadplus.application.tenantcontacts.TenantContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/tenants/{tenantId}/contacts")
@Tag(name = "Tenant Contact APIs", description = "APIs for listing CRM-synced tenant contacts")
public class TenantContactController {

    private final TenantContactService tenantContactService;
    private final TenantValidator tenantValidator;

    @Operation(summary = "Search CRM-synced contacts for a tenant")
    @GetMapping
    public ResponseEntity<Page<TenantContactResponse>> searchContacts(
            @PathVariable Long tenantId,
            @RequestParam(required = false) String query,
            Pageable pageable) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[GET] Search CRM contacts for tenantId: {}", tenantId);
        Page<TenantContactDto> tenantContactDtos = tenantContactService.searchContacts(tenantId, query, pageable);
        Page<TenantContactResponse> response = tenantContactDtos.map(TenantContactResponse::fromDto);
        log.info("[GET] Retrieved CRM contacts for tenantId: {}", tenantId);
        return ResponseEntity.ok(response);
    }
}
