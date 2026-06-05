package ai.leadplus.api.v1.admin.tenant;

import ai.leadplus.api.v1.tenants.TenantUserResponse;
import ai.leadplus.application.tenants.TenantAnalyticsDto;
import ai.leadplus.application.tenants.TenantAnalyticsService;
import ai.leadplus.application.tenants.TenantService;
import ai.leadplus.application.tenants.TenantUserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/tenants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tenants", description = "Tenant management APIs for admin")
public class AdminTenantController {

    private final TenantService tenantService;
    private final TenantAnalyticsService tenantAnalyticsService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get All Tenants - Admin Only")
    @GetMapping
    public ResponseEntity<Page<AdminTenantListingResponse>> getAllTenantsForAdmin(
            @RequestParam(required = false) String query,
            Pageable pageable) {
        log.info("[GET] Admin request to fetch all tenants with search: {}", query);
        Page<AdminTenantListingResponse> responses = tenantService.getAllTenants(query, pageable)
                .map(AdminTenantListingResponse::fromDto);
        log.info("[GET] Fetched {} tenants for admin", responses.getTotalElements());
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Tenant Users - Admin Only")
    @GetMapping("/{tenantId}/users")
    public ResponseEntity<Page<TenantUserResponse>> getTenantUsers(
            @PathVariable Long tenantId,
            @RequestParam(required = false) String query,
            Pageable pageable) {
        log.info("[GET] Admin request to fetch users for tenant: {}", tenantId);
        Page<TenantUserDto> tenantUserDtos = tenantService.getAllUsers(tenantId, query, pageable);
        Page<TenantUserResponse> tenantUserResponses = tenantUserDtos.map(TenantUserResponse::fromDto);
        log.info("[GET] Fetched {} users for tenant: {}", tenantUserResponses.getTotalElements(), tenantId);
        return ResponseEntity.ok(tenantUserResponses);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Tenant Analytics - Admin Only")
    @GetMapping("/{tenantId}/analytics")
    public ResponseEntity<AdminTenantAnalyticsResponse> getTenantAnalytics(@PathVariable Long tenantId) {
        log.info("[GET] Admin request to fetch analytics for tenant: {}", tenantId);
        TenantAnalyticsDto analyticsDto = tenantAnalyticsService.getAnalytics(tenantId);
        AdminTenantAnalyticsResponse response = AdminTenantAnalyticsResponse.fromDto(analyticsDto);
        log.info("[GET] Fetched analytics for tenant: {}", tenantId);
        return ResponseEntity.ok(response);
    }
}
