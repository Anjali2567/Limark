package ai.leadplus.api.v1.leads.contacts;

import ai.leadplus.api.common.TenantValidator;
import ai.leadplus.application.leadcontactevents.LeadContactEventDetailedDto;
import ai.leadplus.application.leadcontactevents.LeadContactEventService;
import ai.leadplus.domain.leadcontactevents.LeadContactEventCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/tenants/{tenantId}/workspaces/{workspaceId}/leads/contacts/{contactId}/events")
@RequiredArgsConstructor
@Tag(name = "Lead Contact Events", description = "APIs for managing Lead Contact Events")
public class LeadContactEventController {

    private final LeadContactEventService leadContactEventService;
    private final TenantValidator tenantValidator;

    @Operation(summary = "Get events for a contact")
    @GetMapping
    public ResponseEntity<Page<LeadContactEventResponse>> getEvents(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @PathVariable String contactId,
            @RequestParam(required = false) LeadContactEventCategory category,
            Pageable pageable) {
        tenantValidator.validateWorkspaceByTenantId(tenantId, workspaceId);
        log.info("[GET] Get events for contactId={} tenantId={} workspaceId={} category={} page={}",
                contactId, tenantId, workspaceId, category, pageable);
        Page<LeadContactEventDetailedDto> leadContactEventDtoPage = leadContactEventService.getByTenantWorkspaceAndContact(
                tenantId, workspaceId, Long.parseLong(contactId), category, pageable);
        Page<LeadContactEventResponse> responses = leadContactEventDtoPage
                .map(LeadContactEventResponse::fromDto);
        log.info("[GET] Returning {} events for contactId={}", responses.getNumberOfElements(), contactId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get counts of events by category for a contact")
    @GetMapping("/counts")
    public ResponseEntity<List<LeadContactEventCountResponse>> getCounts(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @PathVariable String contactId) {
        tenantValidator.validateWorkspaceByTenantId(tenantId, workspaceId);
        log.info("[GET] Get event counts for contactId={} tenantId={} workspaceId={}", contactId, tenantId, workspaceId);
        List<LeadContactEventCountResponse> responses = leadContactEventService.getCountsByTenantWorkspaceAndContact(tenantId, workspaceId, Long.parseLong(contactId)).stream()
                .map(LeadContactEventCountResponse::fromDto)
                .toList();
        log.info("[GET] Returning {} category counts for contactId={}", responses.size(), contactId);
        return ResponseEntity.ok(responses);
    }
}
