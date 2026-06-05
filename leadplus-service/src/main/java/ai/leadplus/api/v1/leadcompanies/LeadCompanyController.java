package ai.leadplus.api.v1.leadcompanies;

import ai.leadplus.application.common.Recency;
import ai.leadplus.application.leadcompany.LeadCompanyDto;
import ai.leadplus.application.leadcompany.LeadCompanySearchService;
import ai.leadplus.application.leadcompany.LeadCompanyService;
import ai.leadplus.application.leadcompany.LeadCompanyWithCountDto;
import ai.leadplus.application.leadcontact.LeadContactService;
import ai.leadplus.domain.leadcompanies.EmployeeRange;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Company", description = "Company APIs")
public class LeadCompanyController {

    private final LeadCompanyService leadCompanyService;
    private final LeadContactService leadContactService;
    private final LeadCompanySearchService leadCompanySearchService;

    @Operation(summary = "Create New Company")
    @PostMapping
    public ResponseEntity<LeadCompanyResponse> createCompany(@RequestBody @Valid LeadCompanyRequest leadCompanyRequest) {
        log.info("[POST] Received request to create new Company");
        LeadCompanyDto leadCompanyDto = leadCompanyService.createCompany(leadCompanyRequest.toDto());
        LeadCompanyResponse leadCompanyResponse = LeadCompanyResponse.toResponse(leadCompanyDto);
        log.info("[POST] Created Company with ID: {}", leadCompanyResponse.getId());
        return new ResponseEntity<>(leadCompanyResponse, HttpStatus.CREATED);
    }

    @Operation(summary = "Search All Companies")
    @GetMapping
    public ResponseEntity<Page<LeadCompanySearchResponse>> searchCompanies(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) List<String> segments,
            Pageable pageable) {
        log.info("[GET] Received request to Search All Companies");
        Page<LeadCompanyDto> leadCompanyDtos = leadCompanyService.searchCompanies(searchText, segments, pageable);

        Map<Long, Long> contactCounts = getContactCounts(leadCompanyDtos);
        Page<LeadCompanySearchResponse> leadCompanyResponses = leadCompanyDtos
                .map(dto -> LeadCompanySearchResponse.toResponse(dto, contactCounts.getOrDefault(dto.getId(), 0L)));

        log.info("[GET] Returning Search {} Companies ", leadCompanyResponses.getTotalElements());
        return ResponseEntity.ok(leadCompanyResponses);
    }

    @Operation(summary = "Search All Companies and counts")
    @GetMapping("/search")
    public ResponseEntity<LeadCompanySearchWithAnalyticsResponse> searchCompaniesWithCounts(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) List<String> segments,
            Pageable pageable) {
        log.info("[GET] Received request to Search All Companies with counts");
        LeadCompanyWithCountDto leadCompanyWithCountDto = leadCompanySearchService.searchLeadCompaniesWithCounts(searchText, segments, pageable);
        LeadCompanySearchWithAnalyticsResponse response = LeadCompanySearchWithAnalyticsResponse.fromDto(leadCompanyWithCountDto);
        log.info("[GET] Returning Search Companies and Counts");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Company by ID Or Domain")
    @GetMapping("/{id}")
    public ResponseEntity<LeadCompanyResponse> getCompanyByIdOrDomain(@PathVariable String id) {
        log.info("[GET] Received request to get Company with ID: {}", id);
        LeadCompanyDto leadCompanyDto = leadCompanyService.getCompanyByIdOrDomain(id);
        log.info("[GET] /{} - Returning Company with ID: {}", id, id);
        return ResponseEntity.ok(LeadCompanyResponse.toResponse(leadCompanyDto));
    }

    @Operation(summary = "Get companies updated after specified timestamp")
    @GetMapping("/updated-at")
    public ResponseEntity<Page<LeadCompanyResponse>> getAllCompaniesByUpdatedAt(
            @RequestParam LocalDateTime updatedAt,
            Pageable pageable) {
        log.info("[GET] Received request to get Companies updated after: {}", updatedAt);
        Page<LeadCompanyDto> leadCompanyDtoPage = leadCompanyService.getAllCompaniesByUpdatedAt(updatedAt, pageable);
        Page<LeadCompanyResponse> response = leadCompanyDtoPage.map(LeadCompanyResponse::toResponse);
        log.info("[GET] Returning {} Companies updated after: {}", response.getTotalElements(), updatedAt);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Company Or Domain")
    @PutMapping("/{id}")
    public ResponseEntity<LeadCompanyResponse> updateCompanyByIdOrDomain(@PathVariable String id,
                                                                         @RequestBody @Valid LeadCompanyRequest leadCompanyRequest) {
        log.info("[PUT] Received request to update Company with Id: {}", id);
        LeadCompanyDto leadCompanyDto = leadCompanyService.updateCompanyByIdOrDomain(id, leadCompanyRequest.toDto());
        LeadCompanyResponse leadCompanyResponse = LeadCompanyResponse.toResponse(leadCompanyDto);
        log.info("[PUT] Updated Company with Id: {}", id);
        return ResponseEntity.ok(leadCompanyResponse);
    }

    @Operation(summary = "Search LeadCompanies by SalespersonId")
    @PreAuthorize("hasRole('LEAD_AGENT_SALES_PROFESSIONAL')")
    @GetMapping("/salesperson/{salespersonId}")
    public ResponseEntity<Page<LeadCompanySearchResponse>> searchLeadCompaniesBySalespersonId(
            @PathVariable String salespersonId,
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) List<String> segments,
            @RequestParam(required = false) List<String> regions,
            @RequestParam(required = false) String employeeRange,
            @RequestParam(required = false) Recency recency,
            @RequestParam(required = false) List<String> states,
            Pageable pageable) {
        log.info("[GET] Received request to search LeadCompanies by SalespersonId: {}", salespersonId);
        EmployeeRange range = EmployeeRange.fromString(employeeRange);
        Page<LeadCompanyDto> companies = leadCompanyService.searchLeadCompaniesBySalespersonIds(List.of(salespersonId), searchText, segments, regions, range, recency, states, pageable);
        Map<Long, Long> contactCounts = getContactCounts(companies);
        Page<LeadCompanySearchResponse> leadCompanyResponses = companies
                .map(dto -> LeadCompanySearchResponse.toResponse(dto, contactCounts.getOrDefault(dto.getId(), 0L)));
        log.info("[GET] Retrieved {} LeadCompanies for SalespersonId: {}", leadCompanyResponses.getTotalElements(), salespersonId);
        return ResponseEntity.ok(leadCompanyResponses);
    }

    @Operation(summary = "Get LeadCompanies for Vice President of Sales")
    @GetMapping("/vp")
    public ResponseEntity<Page<LeadCompanyResponse>> searchLeadCompaniesForVicePresident(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) List<String> segments,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<String> cities,
            @RequestParam(required = false) List<String> countries,
            @RequestParam(required = false) List<String> regions,
            @RequestParam(required = false) String employeeRange,
            @RequestParam(required = false) Recency recency,
            @RequestParam(required = false) Boolean salesPersonAssigned,
            Pageable pageable) {
        log.info("[GET] Received request to get LeadCompanies for Vice President");
        EmployeeRange range = EmployeeRange.fromString(employeeRange);
        Page<LeadCompanyDto> companies = leadCompanyService.searchLeadCompaniesForVicePresident(searchText,
                range,
                recency,
                salesPersonAssigned,
                segments,
                states,
                cities,
                countries,
                regions,
                pageable
        );
        Page<LeadCompanyResponse> responses = companies.map(LeadCompanyResponse::toResponse);
        log.info("[GET] Retrieved {} LeadCompanies for Vice President.", responses.getTotalElements());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Delete Company by ID Or Domain")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompanyByIdOrDomain(@PathVariable String id) {
        log.info("[DELETE] Received request to delete Company with ID: {}", id);
        leadCompanyService.deleteCompanyByIdOrDomain(id);
        log.info("[DELETE] /{} - Deleted Company with ID: {}", id, id);
        return ResponseEntity.noContent().build();
    }

    private Map<Long, Long> getContactCounts(Page<LeadCompanyDto> leadCompanyDtos) {
        List<Long> companyIds = leadCompanyDtos.stream()
                .map(LeadCompanyDto::getId)
                .toList();

        return leadContactService.getContactCounts(companyIds);
    }
}

