package ai.leadplus.api.v1.leadcontacts;

import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.application.leadcontact.LeadContactListDto;
import ai.leadplus.application.leadcontact.LeadContactSearchService;
import ai.leadplus.application.leadcontact.LeadContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "Contact", description = "Contact APIs")
public class LeadContactController {

    private final LeadContactService leadContactService;
    private final LeadContactSearchService leadContactSearchService;

    @Operation(summary = "Search All Contacts")
    @GetMapping("/contacts")
    public ResponseEntity<Page<LeadContactListResponse>> searchContacts(
            @RequestParam(required = false) String query,
            Pageable pageable){
        log.info("[GET] Search Contacts");
        Page<LeadContactListDto> leadContactListDtoPage = leadContactSearchService.searchAllLeads(query, pageable);
        Page<LeadContactListResponse> responses = leadContactListDtoPage.map(LeadContactListResponse::fromDto);
        log.info("[GET] Returning Searched Contacts");
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get contacts updated after specified timestamp")
    @GetMapping("/contacts/updated-at")
    public ResponseEntity<Page<LeadContactResponse>> getAllContactsByUpdatedAt(
            @RequestParam LocalDateTime updatedAt,
            Pageable pageable) {
        log.info("[GET] Received request to get Contacts updated after: {}", updatedAt);
        Page<LeadContactDto> leadContactDtoPage = leadContactService.getAllContactsByUpdatedAt(updatedAt, pageable);
        Page<LeadContactResponse> response = leadContactDtoPage.map(LeadContactResponse::toResponse);
        log.info("[GET] Returning {} Contacts updated after: {}", response.getTotalElements(), updatedAt);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create New Contact")
    @PostMapping("/contacts")
    public ResponseEntity<LeadContactResponse> createContactForDomain(@Valid @RequestBody LeadContactWithDomainRequest request) {
      log.info("[POST] Create Contact for companyId Or Domain ={}", request.getDomain());
      LeadContactDto leadContactDto = leadContactService.createContact(request.getDomain(), request.toDto());
      log.info("[POST] Created Contact for companyId Or Domain ={}", request.getDomain());
      return new ResponseEntity<>(LeadContactResponse.toResponse(leadContactDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Create New Contact")
    @PostMapping("/companies/{companyIdOrDomain}/contacts")
    public ResponseEntity<LeadContactResponse> createContact(@PathVariable String companyIdOrDomain,
                                                             @RequestBody LeadContactRequest request) {
        log.info("[POST] Create Contact with companyId Or Domain ={}", companyIdOrDomain);
        LeadContactDto leadContactDto = leadContactService.createContact(companyIdOrDomain, request.toDto());
        log.info("[POST] Created Contact with companyId Or Domain ={}", companyIdOrDomain);
        return new ResponseEntity<>(LeadContactResponse.toResponse(leadContactDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Search Contacts")
    @GetMapping("/companies/{companyIdOrDomain}/contacts")
    public ResponseEntity<List<LeadContactResponse>> searchContacts(
            @RequestParam(required = false) String searchText,
            @PathVariable String companyIdOrDomain) {
        log.info("[GET] Search Contacts for companyId Or Domain = {}", companyIdOrDomain);
        List<LeadContactResponse> responses = leadContactService.searchContacts(searchText, companyIdOrDomain)
                .stream()
                .map(LeadContactResponse::toResponse).toList();
        log.info("[GET] Returning Searched Contacts for companyId Or Domain = {}", companyIdOrDomain);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get Contact by ID")
    @GetMapping("/companies/{companyIdOrDomain}/contacts/{id}")
    public ResponseEntity<LeadContactResponse> getContactById(@PathVariable String companyIdOrDomain,
                                                              @PathVariable Long id) {
        log.info("[GET] Contact id={} for companyId Or Domain = {}", id, companyIdOrDomain);
        LeadContactDto dto = leadContactService.getContactByIdAndCompanyIdOrDomain(companyIdOrDomain, id);
        log.info("[GET] Returning Contact id={} for companyId Or Domain = {}", id, companyIdOrDomain);
        return ResponseEntity.ok(LeadContactResponse.toResponse(dto));
    }

    @Operation(summary = "Update Contact")
    @PutMapping("/companies/{companyIdOrDomain}/contacts/{id}")
    public ResponseEntity<LeadContactResponse> updateContact(@PathVariable String companyIdOrDomain,
                                                             @PathVariable Long id,
                                                             @RequestBody LeadContactRequest request) {
        log.info("[PUT] Update Contact id={} for companyId Or Domain = {}", id, companyIdOrDomain);
        LeadContactDto dto = leadContactService.updateContact(companyIdOrDomain, id, request.toDto());
        log.info("[PUT] Updated Contact id={} for companyId Or Domain = {}", id, companyIdOrDomain);
        return ResponseEntity.ok(LeadContactResponse.toResponse(dto));
    }

    @Operation(summary = "Delete Contact")
    @DeleteMapping("/companies/{companyIdOrDomain}/contacts/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable String companyIdOrDomain,
                                              @PathVariable Long id) {
        log.info("[DELETE] Delete Contact id={} for companyId Or Domain = {}", id, companyIdOrDomain);
        leadContactService.deleteContact(companyIdOrDomain, id);
        log.info("[DELETE] Deleted Contact id={} for companyId Or Domain = {}", id, companyIdOrDomain);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Update Contact Notes")
    @PutMapping("/companies/{companyId}/contacts/{id}/notes")
    public ResponseEntity<LeadContactResponse> updateContactNotes(@PathVariable Long companyId,
                                                                  @PathVariable Long id,
                                                                  @RequestBody LeadContactNotesRequest request) {
        log.info("[PUT] Update Contact Notes id={} for companyId = {}", id, companyId);
        LeadContactDto leadContactDto = leadContactService.updateNotes(id, companyId, request.getNotes());
        log.info("[PUT] Updated Contact Notes id={} for companyId = {}", id, companyId);
        return ResponseEntity.ok(LeadContactResponse.toResponse(leadContactDto));
    }
}

