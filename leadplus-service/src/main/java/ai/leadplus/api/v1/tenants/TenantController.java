package ai.leadplus.api.v1.tenants;

import ai.leadplus.api.common.TenantValidator;
import ai.leadplus.api.v1.auth.IdentityAuthRequest;
import ai.leadplus.api.v1.leadcontacts.LeadContactResponse;
import ai.leadplus.application.contactoutreachstatuses.ContactOutreachStatusService;
import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.application.tenants.TenantDto;
import ai.leadplus.application.tenants.TenantService;
import ai.leadplus.application.tenants.TenantUserDto;
import ai.leadplus.application.tenants.TenantWorkspaceDto;
import ai.leadplus.domain.tenants.Module;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant API", description = "APIs for Tenant Management")
public class TenantController {

    private final TenantService tenantService;
    private final TenantValidator tenantValidator;
    private final ContactOutreachStatusService contactOutreachStatusService;

    @PreAuthorize("hasRole('TENANT_OWNER')")
    @Operation(summary = "Get Tenant Details")
    @GetMapping("/{tenantId}")
    public ResponseEntity<TenantResponse> getTenantDetails(@PathVariable Long tenantId) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[GET] Received request to fetch tenant details for TenantId: {}", tenantId);
        TenantDto tenantDto = tenantService.getTenant(tenantId);
        TenantResponse tenantResponse = TenantResponse.fromDto(tenantDto);
        log.info("[GET] Fetched tenant details for TenantId: {}", tenantId);
        return ResponseEntity.ok(tenantResponse);
    }

    @Operation(summary = "Get Tenant Recipient Details")
    @GetMapping("/{tenantId}/recipients")
    public ResponseEntity<TenantRecipientResponse> getTenantRecipients(@PathVariable Long tenantId) {
        tenantValidator.validateAuthenticatedUserByTenantId(tenantId);
        log.info("[GET] Received request to fetch tenant recipients for TenantId: {}", tenantId);
        TenantDto tenantDto = tenantService.getTenant(tenantId);
        TenantRecipientResponse tenantResponse = TenantRecipientResponse.fromDto(tenantDto);
        log.info("[GET] Fetched tenant recipients for TenantId: {}", tenantId);
        return ResponseEntity.ok(tenantResponse);
    }

    @PreAuthorize("hasRole('TENANT_OWNER')")
    @Operation(summary = "Connect Zoho CRM")
    @PostMapping("/{tenantId}/connect/zoho")
    public ResponseEntity<TenantResponse> connectZohoCRM(@PathVariable Long tenantId, @RequestBody IdentityAuthRequest request) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[POST] Received request to connect Zoho CRM for TenantId: {}", tenantId);
        TenantDto tenantDto = tenantService.connectZohoCRM(tenantId, request.getCode(), request.getRedirectUri());
        TenantResponse tenantResponse = TenantResponse.fromDto(tenantDto);
        log.info("[POST] Connected Zoho CRM for TenantId: {}", tenantId);
        return ResponseEntity.ok(tenantResponse);
    }

    @PreAuthorize("hasRole('TENANT_OWNER')")
    @Operation(summary = "Disconnect Zoho CRM")
    @DeleteMapping("/{tenantId}/disconnect/zoho")
    public ResponseEntity<TenantResponse> disconnectZohoCRM(@PathVariable Long tenantId) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[DELETE] Received request to disconnect Zoho CRM for TenantId: {}", tenantId);
        TenantDto tenantDto = tenantService.disconnectZohoCRM(tenantId);
        TenantResponse tenantResponse = TenantResponse.fromDto(tenantDto);
        log.info("[DELETE] Disconnected Zoho CRM for TenantId: {}", tenantId);
        return ResponseEntity.ok(tenantResponse);
    }

    @PreAuthorize("hasRole('TENANT_OWNER')")
    @Operation(summary = "Sync Zoho Contacts & Companies")
    @PostMapping("/{tenantId}/sync/zoho")
    public ResponseEntity<Void> syncZohoRecords(@PathVariable Long tenantId) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[POST] Received Zoho record sync request for TenantId: {}", tenantId);
        tenantService.syncZohoRecords(tenantId);
        log.info("[POST] Zoho record sync triggered for TenantId: {}", tenantId);
        return ResponseEntity.accepted().build();
    }

    @PreAuthorize("hasRole('TENANT_OWNER')")
    @Operation(summary = "Connect Hubspot CRM")
    @PostMapping("/{tenantId}/connect/hubspot")
    public ResponseEntity<TenantResponse> connectHubspotCRM(@PathVariable Long tenantId, @RequestBody IdentityAuthRequest request) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[POST] Received request to connect Hubspot CRM for TenantId: {}", tenantId);
        TenantDto tenantDto = tenantService.connectHubSpotCRM(tenantId, request.getCode(), request.getRedirectUri());
        TenantResponse tenantResponse = TenantResponse.fromDto(tenantDto);
        log.info("[POST] Connected Hubspot CRM for TenantId: {}", tenantId);
        return ResponseEntity.ok(tenantResponse);
    }

    @PreAuthorize("hasRole('TENANT_OWNER')")
    @Operation(summary = "Disconnect Hubspot CRM")
    @DeleteMapping("/{tenantId}/disconnect/hubspot")
    public ResponseEntity<TenantResponse> disconnectHubspotCRM(@PathVariable Long tenantId) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[DELETE] Received request to disconnect Hubspot CRM for TenantId: {}", tenantId);
        TenantDto tenantDto = tenantService.disconnectHubSpotCRM(tenantId);
        TenantResponse tenantResponse = TenantResponse.fromDto(tenantDto);
        log.info("[DELETE] Disconnected Hubspot CRM for TenantId: {}", tenantId);
        return ResponseEntity.ok(tenantResponse);
    }

    @PreAuthorize("hasRole('TENANT_OWNER')")
    @Operation(summary = "Sync Hubspot Contacts & Companies")
    @PostMapping("/{tenantId}/sync/hubspot")
    public ResponseEntity<Void> syncHubspotRecords(@PathVariable Long tenantId) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[POST] Received Hubspot record sync request for TenantId: {}", tenantId);
        tenantService.syncHubSpotRecords(tenantId);
        log.info("[POST] Hubspot record sync triggered for TenantId: {}", tenantId);
        return ResponseEntity.accepted().build();
    }

    @PreAuthorize("hasRole('TENANT_OWNER')")
    @Operation(summary = "Get All Workspaces in the tenant")
    @GetMapping("/{tenantId}/workspaces")
    public ResponseEntity<Page<TenantWorkspaceResponse>> getAllWorkspaces(@PathVariable Long tenantId,
                                                                          @RequestParam(required = false) String query,
                                                                          Pageable pageable) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[GET] Received request to get all Workspaces in the tenant: {}", tenantId);
        Page<TenantWorkspaceDto> tenantWorkspaceDtoPage = tenantService.getAllWorkspaces(tenantId, query, pageable);
        Page<TenantWorkspaceResponse> responses = tenantWorkspaceDtoPage.map(TenantWorkspaceResponse::fromDto);
        log.info("[GET] Fetched all Workspaces in the tenant: {}", tenantId);
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('TENANT_OWNER')")
    @Operation(summary = "Get All Users in the tenant")
    @GetMapping("/{tenantId}/users")
    public ResponseEntity<Page<TenantUserResponse>> getAllUsers(@PathVariable Long tenantId,
                                                                @RequestParam(required = false) String query,
                                                                Pageable pageable) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[GET] Received request to get all Users in the tenant: {}", tenantId);
        Page<TenantUserDto> tenantUserDto = tenantService.getAllUsers(tenantId, query, pageable);
        Page<TenantUserResponse> tenantUserResponse = tenantUserDto.map(TenantUserResponse::fromDto);
        log.info("[GET] Fetched all Users in the tenant: {}", tenantId);
        return ResponseEntity.ok(tenantUserResponse);
    }

    @PreAuthorize("hasRole('TENANT_OWNER')")
    @Operation(summary = "Update tenant recipients", description = "Update the default CC/BCC recipients for the specified tenant")
    @PutMapping("/{tenantId}/recipients")
    public ResponseEntity<TenantResponse> updateTenantRecipients(@PathVariable Long tenantId,
                                                                 @RequestBody @Valid TenantRecipientsRequest tenantRecipientsRequest) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[PUT] Received request to update Tenant Recipients for TenantId: {}", tenantId);
        TenantDto tenantDto = tenantService.updateTenantRecipients(tenantId, tenantRecipientsRequest.toDto());
        TenantResponse tenantResponse = TenantResponse.fromDto(tenantDto);
        log.info("[PUT] Updated Tenant with ID: {}", tenantDto.getId());
        return ResponseEntity.ok(tenantResponse);
    }

    @PreAuthorize("hasRole('TENANT_OWNER')")
    @Operation(summary = "Update tenant profile context")
    @PutMapping("/{tenantId}/profile-context")
    public ResponseEntity<TenantResponse> updateTenantProfileContext(@PathVariable Long tenantId,
                                                                     @RequestBody @Valid TenantProfileContextRequest request) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[PUT] Received request to update tenant profile context for TenantId: {}", tenantId);
        TenantDto tenantDto = tenantService.updateTenantProfileContext(tenantId, request.getProfileContext());
        return ResponseEntity.ok(TenantResponse.fromDto(tenantDto));
    }

    @Operation(summary = "Get Unsubscribed Contacts for Tenant")
    @GetMapping("/{tenantId}/unsubscribed-contacts")
    public ResponseEntity<Page<LeadContactResponse>> getUnsubscribedContacts(@PathVariable Long tenantId,
                                                                              Pageable pageable) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[GET] Received request to fetch unsubscribed contacts for TenantId: {}", tenantId);
        Page<LeadContactDto> contactsPage = contactOutreachStatusService.getUnsubscribedContacts(tenantId, pageable);
        Page<LeadContactResponse> response = contactsPage.map(LeadContactResponse::toResponse);
        log.info("[GET] Fetched unsubscribed contacts for TenantId: {} count: {}", tenantId, response.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get enabled modules for the tenant")
    @GetMapping("/modules")
    public ResponseEntity<TenantModulesResponse> getEnabledModules(@RequestParam(required = false) Long tenantId, HttpServletRequest request) {
        String origin = request.getHeader(HttpHeaders.REFERER);
        log.info("[GET] Received request to get enabled modules for tenant with TenantId: {} and Origin: {}", tenantId, origin);
        List<Module> moduleList;
        if (tenantId != null) {
            tenantValidator.validateAuthenticatedUserByTenantId(tenantId);
            moduleList = tenantService.getEnabledModules(tenantId);
        } else {
            moduleList = tenantService.getEnabledModulesFromOrigin(origin);
        }
        TenantModulesResponse response = new TenantModulesResponse(moduleList);
        log.info("[GET] Fetched enabled modules for tenant with TenantId: {} and Origin: {}", tenantId, origin);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('TENANT_OWNER')")
    @Operation(summary = "Connect SMTP for announcements")
    @PostMapping("/{tenantId}/announcement/smtp")
    public ResponseEntity<TenantResponse> connectSmtp(@PathVariable Long tenantId,
                                                      @RequestBody @Valid TenantAnnouncementSmtpRequest request) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[POST] Received request to connect SMTP for announcements for TenantId: {}", tenantId);
        TenantDto tenantDto = tenantService.updateAnnouncementConfig(tenantId, request.toDto());
        log.info("[POST] Connected SMTP for announcements for TenantId: {}", tenantId);
        return ResponseEntity.ok(TenantResponse.fromDto(tenantDto));
    }
}
