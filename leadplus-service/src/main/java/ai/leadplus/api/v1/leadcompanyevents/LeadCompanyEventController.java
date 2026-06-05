package ai.leadplus.api.v1.leadcompanyevents;

import ai.leadplus.application.leadcompanyevent.LeadCompanyEventDto;
import ai.leadplus.application.leadcompanyevent.LeadCompanyEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/companies/{companyIdOrDomain}/events")
@RequiredArgsConstructor
@Tag(name = "Company Events", description = "Company Events APIs")
public class LeadCompanyEventController {

    private final LeadCompanyEventService leadCompanyEventService;

    @Operation(summary = "Create Company Event")
    @PostMapping
    public ResponseEntity<LeadCompanyEventResponse> createEvent(@PathVariable String companyIdOrDomain,
                                                                @RequestBody @Valid LeadCompanyEventRequest request) {
        log.info("[POST] Create event for companyId Or Domain {}", companyIdOrDomain);
        LeadCompanyEventDto dto = leadCompanyEventService.createEvent(companyIdOrDomain, request.toDto());
        log.info("[POST] Created event for companyId Or Domain {}", companyIdOrDomain);
        return new ResponseEntity<>(LeadCompanyEventResponse.toResponse(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Get All Events for Company")
    @GetMapping
    public ResponseEntity<List<LeadCompanyEventResponse>> getEvents(@PathVariable String companyIdOrDomain) {
        log.info("[GET] Get events for companyId Or Domain {}", companyIdOrDomain);
        List<LeadCompanyEventDto> dtos = leadCompanyEventService.getEventsByCompanyIdOrDomain(companyIdOrDomain);
        log.info("[GET] Returning events for companyId Or Domain {}", companyIdOrDomain);
        return ResponseEntity.ok(dtos.stream().map(LeadCompanyEventResponse::toResponse).toList());
    }

    @Operation(summary = "Get Event by ID")
    @GetMapping("/{eventId}")
    public ResponseEntity<LeadCompanyEventResponse> getEventById(@PathVariable String companyIdOrDomain,
                                                                 @PathVariable Long eventId) {
        log.info("[GET] Get eventId {} for companyId Or Domain {}", eventId, companyIdOrDomain);
        LeadCompanyEventDto dto = leadCompanyEventService.getEventById(companyIdOrDomain, eventId);
        log.info("[GET] Returning eventId {} for companyId Or Domain {}", eventId, companyIdOrDomain);
        return ResponseEntity.ok(LeadCompanyEventResponse.toResponse(dto));
    }

    @Operation(summary = "Update Event by ID")
    @PutMapping("/{eventId}")
    public ResponseEntity<LeadCompanyEventResponse> updateEvent(@PathVariable String companyIdOrDomain,
                                                                @PathVariable Long eventId,
                                                                @RequestBody @Valid LeadCompanyEventRequest request) {
        log.info("[PUT] Update eventId {} for companyId Or Domain {}", eventId, companyIdOrDomain);
        LeadCompanyEventDto dto = leadCompanyEventService.updateEvent(companyIdOrDomain, eventId, request.toDto());
        log.info("[PUT] Updated eventId {} for companyId Or Domain {}", eventId, companyIdOrDomain);
        return ResponseEntity.ok(LeadCompanyEventResponse.toResponse(dto));
    }

    @Operation(summary = "Delete Event by ID")
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String companyIdOrDomain,
                                            @PathVariable Long eventId) {
        log.info("[DELETE] Delete eventId {} for companyId Or Domain {}", eventId, companyIdOrDomain);
        leadCompanyEventService.deleteEvent(companyIdOrDomain, eventId);
        log.info("[DELETE] Deleted eventId {} for companyId Or Domain {}", eventId, companyIdOrDomain);
        return ResponseEntity.noContent().build();
    }
}

