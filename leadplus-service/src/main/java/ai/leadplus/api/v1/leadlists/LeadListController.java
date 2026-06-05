package ai.leadplus.api.v1.leadlists;

import ai.leadplus.api.v1.leadcompanies.LeadCompanySearchResponse;
import ai.leadplus.api.v1.leads.LeadResponse;
import ai.leadplus.application.leadcompany.LeadCompanyWithContactCountDto;
import ai.leadplus.application.leadlists.LeadListDto;
import ai.leadplus.application.leadlists.LeadListSearchDto;
import ai.leadplus.application.leadlists.LeadListService;
import ai.leadplus.application.leads.LeadDto;
import ai.leadplus.domain.common.LeadType;
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
@RequestMapping("/v1/tenants/{tenantId}/workspaces/{workspaceId}/lead-lists")
@RequiredArgsConstructor
@Tag(name = "Lead Lists", description = "Lead Lists APIs")
public class LeadListController {

    private final LeadListService leadListService;

    @Operation(summary = "Get Lead Contacts by List ID")
    @GetMapping("/{id}/contacts")
    public ResponseEntity<Page<LeadResponse>> getContactsByListId(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @PathVariable Long id,
            Pageable pageable) {
        log.info("[GET] Get Lead Contacts by listId={} for tenantId={}, workspaceId={}", id, tenantId, workspaceId);
        Page<LeadDto> page = leadListService.getLeadContactsByListId(tenantId, workspaceId, id, pageable);
        Page<LeadResponse> responses = page.map(LeadResponse::fromDto);
        log.info("[GET] Retrieved {} Lead Contacts", responses.getSize());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get Lead Companies by List ID")
    @GetMapping("/{id}/companies")
    public ResponseEntity<Page<LeadCompanySearchResponse>> getCompaniesByListId(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @PathVariable Long id,
            Pageable pageable) {
        log.info("[GET] Get Lead Companies by listId={} for tenantId={}, workspaceId={}", id, tenantId, workspaceId);
        Page<LeadCompanyWithContactCountDto> page = leadListService.getLeadCompaniesByListId(tenantId, workspaceId, id, pageable);
        Page<LeadCompanySearchResponse> responses = page.map(LeadCompanySearchResponse::fromDto);
        log.info("[GET] Retrieved {} Lead Companies", responses.getSize());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Create Lead List")
    @PostMapping
    public ResponseEntity<LeadListResponse> createLeadList(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @Valid @RequestBody LeadListRequest request) {

        log.info("[POST] Create Lead List request received for tenantId={}, workspaceId={}", tenantId, workspaceId);
        LeadListDto saved = leadListService.createLeadList(request.toDto(), tenantId, workspaceId);
        LeadListResponse response = LeadListResponse.fromDto(saved);
        log.info("[POST] Lead List created with id={}", saved.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Lead List by ID")
    @GetMapping("/{id}")
    public ResponseEntity<LeadListResponse> getById(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @PathVariable Long id) {

        log.info("[GET] Get Lead List by id={} for tenantId={}, workspaceId={}", id, tenantId, workspaceId);
        LeadListDto dto = leadListService.getById(id, tenantId, workspaceId);
        LeadListResponse response = LeadListResponse.fromDto(dto);
        log.info("[GET] Lead List retrieved with id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search All Lead List (Optional filter by type)")
    @GetMapping
    public ResponseEntity<Page<LeadListSearchResponse>> searchLeadLists(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) LeadType type,
            Pageable pageable) {

        log.info("[GET] Search all Lead Lists");
        Page<LeadListSearchDto> leadListSearchDtoList = leadListService.searchLeadLists(String.valueOf(tenantId), String.valueOf(workspaceId), query, type, pageable);
        Page<LeadListSearchResponse> responses = leadListSearchDtoList.map(LeadListSearchResponse::fromDto);
        log.info("[GET] Retrieved {} Lead Lists", responses.getSize());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Update Lead List")
    @PutMapping("/{id}")
    public ResponseEntity<LeadListResponse> updateLeadList(
            @PathVariable Long id,
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @Valid @RequestBody LeadListRequest request) {

        log.info("[POST] Update Lead List request received for tenantId={}, workspaceId={}", tenantId, workspaceId);
        LeadListDto saved = leadListService.updateLeadList(request.toDto(), id, tenantId, workspaceId);
        LeadListResponse response = LeadListResponse.fromDto(saved);
        log.info("[POST] Lead List Updated with id={}", saved.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Lead List by ID (Soft Delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @PathVariable Long id) {

        log.info("[DELETE] Delete Lead List by id={} tenantId={} workspaceId={}", id, tenantId, workspaceId);
        leadListService.deleteLeadList(id, tenantId, workspaceId);
        log.info("[DELETE] Lead List soft deleted id={}", id);
        return ResponseEntity.noContent().build();
    }
}
