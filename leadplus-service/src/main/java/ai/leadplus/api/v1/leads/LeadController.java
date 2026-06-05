package ai.leadplus.api.v1.leads;

import ai.leadplus.api.common.UserValidator;
import ai.leadplus.api.v1.common.LeadSearchHistoryRequest;
import ai.leadplus.api.v1.common.LeadSearchHistoryResponse;
import ai.leadplus.api.v1.leadcompanies.LeadCompanySearchResponse;
import ai.leadplus.application.leadcompany.LeadCompanyWithContactCountDto;
import ai.leadplus.application.leadchatservice.LeadChatCompletion;
import ai.leadplus.application.leadchatservice.LeadChatResult;
import ai.leadplus.application.leadchatservice.LeadChatService;
import ai.leadplus.application.leads.CompanyLookupDto;
import ai.leadplus.application.leads.LeadFilterCriteria;
import ai.leadplus.application.leads.LeadDto;
import ai.leadplus.application.leads.LeadStatisticsDto;
import ai.leadplus.application.leads.TenantLeadService;
import ai.leadplus.application.tenantcontactmetadata.TenantContactMetadataService;
import ai.leadplus.application.messages.MessageDto;
import ai.leadplus.application.messages.MessageService;
import ai.leadplus.application.leadsearchhistories.LeadSearchHistoryDto;
import ai.leadplus.application.leadsearchhistories.LeadSearchHistoryService;
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

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/tenants/{tenantId}/leads")
@RequiredArgsConstructor
@Tag(name = "Leads", description = "Leads APIs")
public class LeadController {

    private final LeadSearchHistoryService leadSearchHistoryService;
    private final UserValidator userValidator;
    private final LeadChatService leadChatService;
    private final TenantLeadService tenantLeadService;
    private final TenantContactMetadataService tenantContactMetadataService;
    private final MessageService messageService;

    @GetMapping("/contacts/{contactId}")
    public ResponseEntity<LeadResponse> getContactById(@PathVariable Long tenantId, @PathVariable Long contactId) {
        log.info("[GET] Request to fetch lead contact by ID: {}", contactId);
        LeadDto leadDto = tenantLeadService.getContact(tenantId, contactId);
        LeadResponse leadResponse = LeadResponse.fromDto(leadDto);
        log.info("[GET] Successfully fetched lead contact by ID: {}", leadDto);
        return ResponseEntity.ok(leadResponse);
    }

    @GetMapping("/companies/{idOrDomain}")
    public ResponseEntity<LeadCompanySearchResponse> getCompanyById(@PathVariable Long tenantId, @PathVariable String idOrDomain) {
        log.info("[GET] Request to fetch lead company by ID or Domain: {}", idOrDomain);
        LeadCompanyWithContactCountDto companyDto = tenantLeadService.getCompany(tenantId, idOrDomain);
        LeadCompanySearchResponse leadCompanySearchResponse = LeadCompanySearchResponse.fromDto(companyDto);
        log.info("[GET] Successfully fetched lead company by ID or Domain: {}", companyDto);
        return ResponseEntity.ok(leadCompanySearchResponse);
    }

    @GetMapping("/companies/{idOrDomain}/contacts")
    public ResponseEntity<Page<LeadResponse>> getContactsByCompanyId(@PathVariable Long tenantId, @PathVariable String idOrDomain, Pageable pageable) {
        log.info("[GET] Request to fetch lead contacts by Company ID or Domain: {}", idOrDomain);
        Page<LeadDto> leadDtoList = tenantLeadService.getContactsByCompany(tenantId, idOrDomain, pageable);
        Page<LeadResponse> leadResponses = leadDtoList.map(LeadResponse::fromDto);
        log.info("[GET] Successfully fetched lead contacts by Company ID or Domain: {}, number of contacts found: {}", idOrDomain, leadResponses.getTotalElements());
        return ResponseEntity.ok(leadResponses);
    }

    @Operation(summary = "Search All Lead Contacts")
    @PostMapping("/contacts")
    public ResponseEntity<Page<LeadResponse>> searchAllLeads(
            @PathVariable Long tenantId,
            @RequestBody LeadFilterCriteria leadFilterCriteria,
            @RequestParam(required = false) String query,
            Pageable pageable) {
        log.info("[POST] Received Request to Search Leads");
        Page<LeadDto> page = tenantLeadService.searchContacts(tenantId, leadFilterCriteria, query, leadFilterCriteria.getCompanyIds(), pageable);
        Page<LeadResponse> responses = page.map(LeadResponse::fromDto);
        log.info("[POST] Successfully Processed Request to Search Leads");
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Search All Lead Companies")
    @PostMapping("/companies")
    public ResponseEntity<Page<LeadCompanySearchResponse>> searchAllLeadCompanies(
            @PathVariable Long tenantId,
            @RequestBody LeadFilterCriteria leadFilterCriteria,
            @RequestParam(required = false) String query,
            Pageable pageable) {
        log.info("[POST] Received Request to Search Lead Companies");
        Page<LeadCompanyWithContactCountDto> page = tenantLeadService.searchCompanies(tenantId, leadFilterCriteria, query, pageable);
        Page<LeadCompanySearchResponse> responses = page.map(LeadCompanySearchResponse::fromDto);
        log.info("[POST] Successfully Processed Request to Search Lead Companies");
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Search Contact IDs Only")
    @PostMapping("/contacts/ids")
    public ResponseEntity<List<Long>> searchContactIds(
            @PathVariable Long tenantId,
            @RequestBody LeadFilterCriteria leadFilterCriteria,
            @RequestParam(required = false) String query) {
        log.info("[POST] Received Request to Search Contact IDs");
        List<Long> ids = tenantLeadService.searchContactIds(tenantId, leadFilterCriteria, query, leadFilterCriteria.getCompanyIds());
        log.info("[POST] Successfully fetched {} contact IDs", ids.size());
        return ResponseEntity.ok(ids);
    }

    @Operation(summary = "Search Company IDs with Domains")
    @PostMapping("/companies/ids")
    public ResponseEntity<List<CompanyIdWithDomainResponse>> searchCompanyIdsWithDomains(
            @PathVariable Long tenantId,
            @RequestBody LeadFilterCriteria leadFilterCriteria,
            @RequestParam(required = false) String query) {
        log.info("[POST] Received Request to Search Company IDs");
        List<CompanyIdWithDomainResponse> responses = tenantLeadService
                .searchCompanyIdsWithDomains(tenantId, leadFilterCriteria, query)
                .stream()
                .map(CompanyIdWithDomainResponse::fromDto)
                .toList();
        log.info("[POST] Successfully fetched {} company entries", responses.size());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Lookup companies by name or domain (starts with)")
    @GetMapping("/companies/lookup")
    public ResponseEntity<List<CompanyLookupResponse>> lookupCompanies(
            @PathVariable Long tenantId,
            @RequestParam(required = false, defaultValue = "") String query) {
        log.info("[GET] Received Request to Lookup Companies with query: {}", query);
        List<CompanyLookupDto> results = tenantLeadService.lookupCompanies(tenantId, query);
        List<CompanyLookupResponse> responses = results.stream()
                .map(CompanyLookupResponse::fromDto)
                .toList();
        log.info("[GET] Successfully found {} companies matching query", responses.size());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Parse query with AI")
    @PostMapping("/chat")
    public ResponseEntity<LeadChatResponse> chatLeadQuery(@PathVariable Long tenantId,
                                                          @Valid @RequestBody LeadChatRequest request) {
        log.info("[POST] Received request as: {}", request.getRequest());
        LeadChatResult chatResult = leadChatService.getLeadQueryAttributes(tenantId, Long.valueOf(userValidator.getAuthenticatedUserId()), request);
        LeadChatCompletion completion = chatResult.getCompletion();
        LeadChatResponse response = LeadChatResponse.fromCompletion(completion, request.getRequest());
        response.setConversationId(request.getConversationId());
        response.setMessageId(chatResult.getMessageId());
        log.info("[POST] Successfully parsed user input to lead chat response");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get lead chat messages by conversation")
    @GetMapping("/chat/{conversationId}/messages")
    public ResponseEntity<List<LeadChatMessageResponse>> getLeadChatMessages(@PathVariable Long tenantId,
                                                                              @PathVariable Long conversationId) {
        Long userId = Long.valueOf(userValidator.getAuthenticatedUserId());
        List<MessageDto> messages = messageService.getLeadChatMessagesByConversationId(tenantId, userId, conversationId);
        List<LeadChatMessageResponse> responses = messages.stream()
                .map(LeadChatMessageResponse::fromDto)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get Lead Statistics")
    @GetMapping("/statistics")
    public ResponseEntity<LeadStatisticsResponse> getLeadStatistics(@PathVariable Long tenantId) {
        log.info("[GET] Received Request to Get Lead Statistics");
        LeadStatisticsDto statisticsDto = tenantLeadService.getStatistics(tenantId);
        LeadStatisticsResponse response = LeadStatisticsResponse.fromDto(statisticsDto);
        log.info("[GET] Successfully Processed Request to Get Lead Statistics");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create Lead Search History")
    @PostMapping("/save-search")
    public ResponseEntity<LeadSearchHistoryResponse> createLeadSearchHistory(
            @RequestParam LeadType type,
            @RequestBody LeadSearchHistoryRequest request) {
        log.info("[POST] Received request to create new Contact Lead Search History");
        String userId = userValidator.getAuthenticatedUserId();
        LeadSearchHistoryDto leadSearchHistoryDto = leadSearchHistoryService
                .createLeadSearchHistory(request.toDto(Long.valueOf(userId), type));
        LeadSearchHistoryResponse leadSearchHistoryResponse = LeadSearchHistoryResponse.toResponse(leadSearchHistoryDto);
        log.info("[POST] Created Contact Lead Search History with ID: {}", leadSearchHistoryResponse.getId());
        return ResponseEntity.ok(leadSearchHistoryResponse);
    }

    @Operation(summary = "Get Lead Search History")
    @GetMapping("/save-search")
    public ResponseEntity<List<LeadSearchHistoryResponse>> getLeadSearchHistoryByUserId(@RequestParam LeadType type) {
        log.info("[GET] Received request to get Contact Lead Search History");
        String userId = userValidator.getAuthenticatedUserId();
        List<LeadSearchHistoryDto> leadSearchHistoryDtos = leadSearchHistoryService.getLeadSearchHistoryByUserId(Long.valueOf(userId), type);
        List<LeadSearchHistoryResponse> leadSearchHistoryResponses = leadSearchHistoryDtos
                .stream()
                .map(LeadSearchHistoryResponse::toResponse)
                .toList();
        log.info("[GET] Returning {} Contact Lead Search History", leadSearchHistoryResponses.size());
        return ResponseEntity.ok(leadSearchHistoryResponses);
    }

@Operation(summary = "Get all distinct metadata filter values for a tenant")
    @GetMapping("/metadata-filters")
    public ResponseEntity<Map<String, List<String>>> getMetadataFilters(@PathVariable Long tenantId) {
        log.info("[GET] Received request to get all metadata filter values for tenant: {}", tenantId);
        Map<String, List<String>> values = tenantContactMetadataService.getAllDistinctValues(tenantId);
        return ResponseEntity.ok(values);
    }
}
