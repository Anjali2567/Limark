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
@RequestMapping("/v1/tenants/{tenantId}/leads/contacts/{contactId}/events")
@RequiredArgsConstructor
@Tag(name = "Lead Contact Events Tenant", description = "APIs for Lead Contact Events at Tenant Level")
public class LeadContactEventTenantController {

    private final LeadContactEventService leadContactEventService;
    private final TenantValidator tenantValidator;

    @Operation(summary = "Get Lead Contact Events by Tenant ID And Contact Id")
    @GetMapping
    public ResponseEntity<Page<LeadContactEventResponse>> getEventsByTenantIdAndContactId(
            @PathVariable Long tenantId,
            @PathVariable String contactId,
            @RequestParam(required = false) LeadContactEventCategory category,
            Pageable pageable) {

        tenantValidator.validateAuthenticatedUserByTenantId(tenantId);

        log.info("[GET] Fetch LeadContactEvents by tenantId={}, contactId={} page={}", tenantId, contactId, pageable);
        Page<LeadContactEventDetailedDto> leadContactEventDtoPage = leadContactEventService.getEventsByTenantIdAndContactId(
                tenantId, Long.parseLong(contactId), category, pageable);
        Page<LeadContactEventResponse> responses = leadContactEventDtoPage
                .map(LeadContactEventResponse::fromDto);
        log.info("[GET] Returning {} events for tenantId={} , contactId={}",
                responses.getNumberOfElements(), tenantId, contactId);

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get counts of events by category for a contact")
    @GetMapping("/counts")
    public ResponseEntity<List<LeadContactEventCountResponse>> getCounts(
            @PathVariable Long tenantId,
            @PathVariable String contactId) {
        tenantValidator.validateAuthenticatedUserByTenantId(tenantId);

        log.info("[GET] Get event counts for contactId={} tenantId={}", contactId, tenantId);
        List<LeadContactEventCountResponse> responses = leadContactEventService.getCountsByTenantAndContact(tenantId, Long.parseLong(contactId)).stream()
                .map(LeadContactEventCountResponse::fromDto)
                .toList();
        log.info("[GET] Returning {} category counts for contactId={}", responses.size(), contactId);
        return ResponseEntity.ok(responses);
    }
}
