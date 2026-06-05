package ai.leadplus.api.v1.tenantannouncements;

import ai.leadplus.api.common.TenantValidator;
import ai.leadplus.application.tenantannouncementcontacts.TenantAnnouncementContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/tenants/{tenantId}/announcements/{announcementId}/contacts")
@Tag(name = "Announcement Contact APIs", description = "APIs for importing and managing announcement contacts")
public class TenantAnnouncementContactController {

    private final TenantAnnouncementContactService tenantAnnouncementContactService;
    private final TenantValidator tenantValidator;

    @Operation(summary = "Import LeadContacts into announcement")
    @PostMapping("/import/leads")
    public ResponseEntity<Void> importLeadContacts(
            @PathVariable Long tenantId,
            @PathVariable Long announcementId,
            @Valid @RequestBody ImportLeadContactsRequest request) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[POST] Importing {} lead contacts into announcementId: {}", request.getLeadContactIds().size(), announcementId);
        tenantAnnouncementContactService.importLeadContacts(tenantId, announcementId, request.getLeadContactIds());
        log.info("[POST] Lead contacts imported for announcementId: {}", announcementId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Import CRM (TenantContacts) into announcement")
    @PostMapping("/import/crm")
    public ResponseEntity<Void> importCrmContacts(
            @PathVariable Long tenantId,
            @PathVariable Long announcementId,
            @Valid @RequestBody ImportCrmContactsRequest request) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[POST] Importing {} CRM contacts into announcementId: {}", request.getTenantContactIds().size(), announcementId);
        tenantAnnouncementContactService.importCrmContacts(tenantId, announcementId, request.getTenantContactIds());
        log.info("[POST] CRM contacts imported for announcementId: {}", announcementId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List announcement contacts (paginated)")
    @GetMapping
    public ResponseEntity<Page<TenantAnnouncementContactResponse>> listContacts(
            @PathVariable Long tenantId,
            @PathVariable Long announcementId,
            Pageable pageable) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[GET] Listing contacts for announcementId: {}", announcementId);
        Page<TenantAnnouncementContactResponse> response = tenantAnnouncementContactService
                .listContacts(tenantId, announcementId, pageable)
                .map(TenantAnnouncementContactResponse::fromDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove a single contact from announcement (DRAFT only)")
    @DeleteMapping("/{contactId}")
    public ResponseEntity<Void> removeContact(
            @PathVariable Long tenantId,
            @PathVariable Long announcementId,
            @PathVariable Long contactId) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[DELETE] Removing contactId: {} from announcementId: {}", contactId, announcementId);
        tenantAnnouncementContactService.removeContact(tenantId, announcementId, contactId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Clear all contacts from announcement (DRAFT only)")
    @DeleteMapping
    public ResponseEntity<Void> clearContacts(
            @PathVariable Long tenantId,
            @PathVariable Long announcementId) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[DELETE] Clearing all contacts for announcementId: {}", announcementId);
        tenantAnnouncementContactService.clearContacts(tenantId, announcementId);
        return ResponseEntity.noContent().build();
    }
}
