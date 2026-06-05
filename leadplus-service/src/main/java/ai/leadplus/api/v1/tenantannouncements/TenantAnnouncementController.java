package ai.leadplus.api.v1.tenantannouncements;

import ai.leadplus.api.common.TenantValidator;
import ai.leadplus.application.tenantannouncements.TenantAnnouncementDto;
import ai.leadplus.application.tenantannouncements.TenantAnnouncementService;
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
@RequestMapping("/v1/tenants/{tenantId}/announcements")
@Tag(name = "Announcement APIs", description = "APIs for Announcement management")
public class TenantAnnouncementController {

    private final TenantAnnouncementService tenantAnnouncementService;
    private final TenantValidator tenantValidator;

    @Operation(summary = "Create announcement")
    @PostMapping
    public ResponseEntity<TenantAnnouncementResponse> createAnnouncement(
            @PathVariable Long tenantId,
            @Valid @RequestBody TenantAnnouncementRequest request) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[POST] Creating announcement for tenantId: {}", tenantId);
        TenantAnnouncementDto dto = tenantAnnouncementService.createAnnouncement(tenantId, request.toDto());
        log.info("[POST] Announcement created with id: {}", dto.getId());
        return ResponseEntity.ok(TenantAnnouncementResponse.fromDto(dto));
    }

    @Operation(summary = "Search announcements")
    @GetMapping
    public ResponseEntity<Page<TenantAnnouncementResponse>> listAnnouncements(
            @PathVariable Long tenantId,
            @RequestParam(required = false) String query,
            Pageable pageable) {
        log.info("[GET] Search announcements for tenantId: {}", tenantId);
        Page<TenantAnnouncementResponse> response = tenantAnnouncementService.listAnnouncements(tenantId, query, pageable)
                .map(TenantAnnouncementResponse::fromDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get announcement by ID")
    @GetMapping("/{announcementId}")
    public ResponseEntity<TenantAnnouncementResponse> getAnnouncement(
            @PathVariable Long tenantId,
            @PathVariable Long announcementId) {
        log.info("[GET] Getting announcement id: {}", announcementId);
        TenantAnnouncementDto dto = tenantAnnouncementService.getAnnouncement(tenantId, announcementId);
        return ResponseEntity.ok(TenantAnnouncementResponse.fromDto(dto));
    }

    @Operation(summary = "Update announcement (DRAFT only)")
    @PutMapping("/{announcementId}")
    public ResponseEntity<TenantAnnouncementResponse> updateAnnouncement(
            @PathVariable Long tenantId,
            @PathVariable Long announcementId,
            @Valid @RequestBody TenantAnnouncementRequest request) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[PUT] Updating announcement id: {}", announcementId);
        TenantAnnouncementDto dto = tenantAnnouncementService.updateAnnouncement(tenantId, announcementId, request.toDto());
        log.info("[PUT] Announcement updated id: {}", announcementId);
        return ResponseEntity.ok(TenantAnnouncementResponse.fromDto(dto));
    }

    @Operation(summary = "Launch announcement (DRAFT → IN_PROGRESS)")
    @PostMapping("/{announcementId}/launch")
    public ResponseEntity<TenantAnnouncementResponse> launchAnnouncement(
            @PathVariable Long tenantId,
            @PathVariable Long announcementId) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[POST] Launching announcement id: {}", announcementId);
        TenantAnnouncementDto dto = tenantAnnouncementService.launchAnnouncement(tenantId, announcementId);
        log.info("[POST] Announcement launched id: {}", announcementId);
        return ResponseEntity.ok(TenantAnnouncementResponse.fromDto(dto));
    }

    @Operation(summary = "Delete announcement (DRAFT only)")
    @DeleteMapping("/{announcementId}")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable Long tenantId,
            @PathVariable Long announcementId) {
        tenantValidator.validateTenantOwner(tenantId);
        log.info("[DELETE] Deleting announcement id: {}", announcementId);
        tenantAnnouncementService.deleteAnnouncement(tenantId, announcementId);
        log.info("[DELETE] Announcement deleted id: {}", announcementId);
        return ResponseEntity.noContent().build();
    }
}
