package ai.leadplus.application.campaignagent.tools;

import ai.leadplus.application.campaignchatmemory.CampaignChatMemoryDto;
import ai.leadplus.application.campaignchatmemory.CampaignChatMemoryService;
import ai.leadplus.application.campaigngenerator.tools.LeadSearchToolResponse;
import ai.leadplus.application.common.TargetingCriteriaDto;
import ai.leadplus.application.contactoutreachstatuses.ContactOutreachStatusService;
import ai.leadplus.application.leaddatapacks.GatedInfo;
import ai.leadplus.application.leaddatapacks.LeadDataPackService;
import ai.leadplus.application.leads.LeadFilterCriteria;
import ai.leadplus.application.vendordatapacks.VendorAccess;
import ai.leadplus.application.vendordatapacks.VendorDataPackService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadSearchAgentServiceTest {

    @Mock
    private CampaignChatMemoryService campaignChatMemoryService;

    @Mock
    private ContactOutreachStatusService contactOutreachStatusService;

    @Mock
    private VendorDataPackService vendorDataPackService;

    @Mock
    private LeadDataPackService leadDataPackService;

    @Mock
    private AgentLeadCountAggregationService agentLeadCountAggregationService;

    @InjectMocks
    private LeadSearchAgentService leadSearchAgentService;

    // -------------------------------------------------------------------------
    // searchLeads
    // -------------------------------------------------------------------------

    @Test
    void searchLeads_shouldReturnEmptyResponse_whenNoContactsFound() {
        Long campaignId = 1L;
        // Failure 1 fix: the service ALWAYS saves the filter to memory after counting
        // (so that "try again" / "broaden it" uses the latest zero-result filter, not
        // a stale previous successful filter). The old test incorrectly asserted
        // never().updateCampaignChatMemory — update to expect the save.
        prepareMockCampaignMemory(campaignId);
        stubDependencies();
        when(agentLeadCountAggregationService.count(any(), any(), any(), any(), any()))
                .thenReturn(emptyCountResult());

        LeadSearchToolResponse response = leadSearchAgentService.searchLeads(
                campaignId,
                null, null, List.of("TX"), null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null
        );

        assertNotNull(response);
        assertEquals(0, response.getTotalCompanies());
        assertEquals(0, response.getTotalContacts());
        assertTrue(response.getSampleCompanyNames().isEmpty());

        // Memory IS saved even on zero results so subsequent turns operate on the
        // correct (latest) filter, not a stale previous successful filter.
        verify(campaignChatMemoryService).updateCampaignChatMemory(any(CampaignChatMemoryDto.class));

        // searchResultId is absent when there are no contacts
        assertNull(response.getSearchResultId());
    }

    @Test
    void searchLeads_shouldSaveFilterToMemory_whenContactsFound() {
        Long campaignId = 1L;
        prepareMockCampaignMemory(campaignId);
        stubDependencies();
        when(agentLeadCountAggregationService.count(any(), any(), any(), any(), any()))
                .thenReturn(countResult(5, 20, List.of("Acme Corp")));

        LeadSearchToolResponse response = leadSearchAgentService.searchLeads(
                campaignId,
                null, null, List.of("TX"), null,
                null, null, null, null,
                null, null, null, List.of("CEO"),
                null, null, null, null, null
        );

        assertNotNull(response);
        assertEquals(5, response.getTotalCompanies());
        assertEquals(20, response.getTotalContacts());
        assertEquals(List.of("Acme Corp"), response.getSampleCompanyNames());
        assertNotNull(response.getSearchResultId()); // present when contacts found

        ArgumentCaptor<CampaignChatMemoryDto> captor = ArgumentCaptor.forClass(CampaignChatMemoryDto.class);
        verify(campaignChatMemoryService).updateCampaignChatMemory(captor.capture());

        CampaignChatMemoryDto saved = captor.getValue();
        assertNotNull(saved.getLeadFilter());
        assertEquals(List.of("TX"), saved.getLeadFilter().getStates());
        assertEquals(List.of("CEO"), saved.getLeadFilter().getTitles());
        assertNull(saved.getTargetingCriteria().getCompanyLocationFilter());
        assertNotNull(saved.getTargetingCriteria().getContactLocationFilter());
    }

    @Test
    void searchLeads_shouldPassLeadFilterCorrectly_toAggregationService() {
        Long campaignId = 1L;
        prepareMockCampaignMemory(campaignId);
        stubDependencies();
        when(agentLeadCountAggregationService.count(any(), any(), any(), any(), any()))
                .thenReturn(countResult(1, 1, List.of()));

        leadSearchAgentService.searchLeads(
                campaignId,
                List.of("Acme"), List.of("Austin"), List.of("TX"), List.of("US"),
                List.of("South"), List.of("CRM"), List.of("SaaS"), List.of("1-10"),
                null, List.of("Salesforce"), null, List.of("VP"),
                List.of("Senior"), List.of("Engineering"), null, null, null
        );

        ArgumentCaptor<LeadFilterCriteria> filterCaptor = ArgumentCaptor.forClass(LeadFilterCriteria.class);
        verify(agentLeadCountAggregationService).count(filterCaptor.capture(), any(), any(), any(), any());

        LeadFilterCriteria capturedFilter = filterCaptor.getValue();
        assertEquals(List.of("Acme"), capturedFilter.getCompanyNames());
        assertEquals(List.of("Austin"), capturedFilter.getCities());
        assertEquals(List.of("TX"), capturedFilter.getStates());
        assertEquals(List.of("US"), capturedFilter.getCountries());
        assertEquals(List.of("South"), capturedFilter.getRegions());
        assertEquals(List.of("CRM"), capturedFilter.getKeywords());
        assertEquals(List.of("SaaS"), capturedFilter.getIndustries());
        assertEquals(List.of("VP"), capturedFilter.getTitles());
        assertTrue(capturedFilter.isAggregateTechSearch());
    }

    // -------------------------------------------------------------------------
    // narrowDownContacts
    // -------------------------------------------------------------------------

    @Test
    void narrowDownContacts_shouldMergeBaseFilterWithNewCriteria() {
        Long campaignId = 1L;
        // buildMemoryWithFilter sets up the mock itself — don't call stubDependencies()
        // separately as that would double-stub updateCampaignChatMemory.
        CampaignChatMemoryDto memoryDto = buildMemoryWithFilter(campaignId,
                LeadFilterCriteria.builder()
                        .companyStates(List.of("TX"))
                        .industries(List.of("SaaS"))
                        .build()
        );
        when(campaignChatMemoryService.getCampaignChatMemoryById(campaignId)).thenReturn(memoryDto);
        stubDependencies();
        when(agentLeadCountAggregationService.count(any(), any(), any(), any(), any()))
                .thenReturn(countResult(3, 10, List.of("Beta Inc")));

        leadSearchAgentService.narrowDownContacts(campaignId, List.of("CTO"), List.of("CA"), null, null);

        ArgumentCaptor<LeadFilterCriteria> filterCaptor = ArgumentCaptor.forClass(LeadFilterCriteria.class);
        verify(agentLeadCountAggregationService).count(filterCaptor.capture(), any(), any(), any(), any());

        LeadFilterCriteria narrowed = filterCaptor.getValue();
        assertEquals(List.of("TX"), narrowed.getCompanyStates());
        assertEquals(List.of("SaaS"), narrowed.getIndustries());
        assertEquals(List.of("CTO"), narrowed.getTitles());
        assertEquals(List.of("CA"), narrowed.getStates());
        assertTrue(narrowed.isAggregateTechSearch());
    }

    @Test
    void narrowDownContacts_shouldUseFallbackEmptyFilter_whenNoBaseFilterInMemory() {
        Long campaignId = 1L;
        CampaignChatMemoryDto memoryDto = new CampaignChatMemoryDto();
        memoryDto.setId(campaignId);
        memoryDto.setTenantId(1L);
        memoryDto.setLeadFilter(null);

        when(campaignChatMemoryService.getCampaignChatMemoryById(campaignId)).thenReturn(memoryDto);
        stubDependencies();
        when(agentLeadCountAggregationService.count(any(), any(), any(), any(), any()))
                .thenReturn(countResult(0, 0, List.of()));

        LeadSearchToolResponse response = leadSearchAgentService.narrowDownContacts(
                campaignId, List.of("CEO"), null, null, null
        );

        assertNotNull(response);
        ArgumentCaptor<LeadFilterCriteria> filterCaptor = ArgumentCaptor.forClass(LeadFilterCriteria.class);
        verify(agentLeadCountAggregationService).count(filterCaptor.capture(), any(), any(), any(), any());
        assertEquals(List.of("CEO"), filterCaptor.getValue().getTitles());
    }

    @Test
    void narrowDownContacts_shouldSaveMemory_afterCounting() {
        Long campaignId = 1L;
        // Failure 3 fix: prepareMockCampaignMemory previously double-stubbed
        // updateCampaignChatMemory — now it only stubs getCampaignChatMemoryById.
        // stubDependencies() handles updateCampaignChatMemory.
        prepareMockCampaignMemory(campaignId);
        stubDependencies();
        when(agentLeadCountAggregationService.count(any(), any(), any(), any(), any()))
                .thenReturn(countResult(2, 8, List.of("Corp A")));

        leadSearchAgentService.narrowDownContacts(campaignId, List.of("Director"), null, null, null);

        verify(campaignChatMemoryService).updateCampaignChatMemory(any(CampaignChatMemoryDto.class));
    }

    // -------------------------------------------------------------------------
    // setContactLimit
    // -------------------------------------------------------------------------

    @Test
    void setContactLimit_shouldReturnError_whenNoLeadFilterExists() {
        Long campaignId = 1L;
        CampaignChatMemoryDto memoryDto = new CampaignChatMemoryDto();
        memoryDto.setId(campaignId);
        memoryDto.setLeadFilter(null);
        when(campaignChatMemoryService.getCampaignChatMemoryById(campaignId)).thenReturn(memoryDto);

        LeadSearchToolResponse response = leadSearchAgentService.setContactLimit(campaignId, 100);

        assertNotNull(response.getMessage());
        assertTrue(response.getMessage().contains("No search has been run yet"));
        verify(campaignChatMemoryService, never()).updateCampaignChatMemory(any());
    }

    @Test
    void setContactLimit_shouldReturnError_whenLimitIsNegative() {
        Long campaignId = 1L;
        CampaignChatMemoryDto memoryDto = new CampaignChatMemoryDto();
        memoryDto.setId(campaignId);
        memoryDto.setLeadFilter(new LeadFilterCriteria());
        when(campaignChatMemoryService.getCampaignChatMemoryById(campaignId)).thenReturn(memoryDto);

        LeadSearchToolResponse response = leadSearchAgentService.setContactLimit(campaignId, -5);

        assertNotNull(response.getMessage());
        assertTrue(response.getMessage().contains("positive number"));
        verify(campaignChatMemoryService, never()).updateCampaignChatMemory(any());
    }

    @Test
    void setContactLimit_shouldReturnWarning_whenLimitExceedsTotalContacts() {
        Long campaignId = 1L;
        CampaignChatMemoryDto memoryDto = buildMemoryWithTargetingCriteria(campaignId, 5L, 50L);
        memoryDto.setLeadFilter(new LeadFilterCriteria());
        when(campaignChatMemoryService.getCampaignChatMemoryById(campaignId)).thenReturn(memoryDto);

        LeadSearchToolResponse response = leadSearchAgentService.setContactLimit(campaignId, 100);

        assertNotNull(response.getMessage());
        assertTrue(response.getMessage().contains("equal to or greater than"));
        verify(campaignChatMemoryService, never()).updateCampaignChatMemory(any());
    }

    @Test
    void setContactLimit_shouldApplyLimit_whenLimitIsValid() {
        Long campaignId = 1L;
        CampaignChatMemoryDto memoryDto = buildMemoryWithTargetingCriteria(campaignId, 10L, 200L);
        memoryDto.setLeadFilter(new LeadFilterCriteria());
        when(campaignChatMemoryService.getCampaignChatMemoryById(campaignId)).thenReturn(memoryDto);
        when(campaignChatMemoryService.updateCampaignChatMemory(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        LeadSearchToolResponse response = leadSearchAgentService.setContactLimit(campaignId, 50);

        assertEquals(50, response.getTotalContacts());
        assertEquals(10, response.getTotalCompanies());

        ArgumentCaptor<CampaignChatMemoryDto> captor = ArgumentCaptor.forClass(CampaignChatMemoryDto.class);
        verify(campaignChatMemoryService).updateCampaignChatMemory(captor.capture());
        assertEquals(50, captor.getValue().getContactLimit());
    }

    @Test
    void setContactLimit_zero_shouldClearLimit_andReturnTotalContacts() {
        Long campaignId = 1L;
        CampaignChatMemoryDto memoryDto = buildMemoryWithTargetingCriteria(campaignId, 10L, 200L);
        memoryDto.setLeadFilter(new LeadFilterCriteria());
        when(campaignChatMemoryService.getCampaignChatMemoryById(campaignId)).thenReturn(memoryDto);
        when(campaignChatMemoryService.updateCampaignChatMemory(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        LeadSearchToolResponse response = leadSearchAgentService.setContactLimit(campaignId, 0);

        assertEquals(200, response.getTotalContacts());
        ArgumentCaptor<CampaignChatMemoryDto> captor = ArgumentCaptor.forClass(CampaignChatMemoryDto.class);
        verify(campaignChatMemoryService).updateCampaignChatMemory(captor.capture());
        assertNull(captor.getValue().getContactLimit());
    }

    // -------------------------------------------------------------------------
    // updateCampaignFiltersAndCount
    // -------------------------------------------------------------------------

    @Test
    void updateCampaignFiltersAndCount_shouldCountAndSaveMemory() {
        Long campaignId = 1L;
        CampaignChatMemoryDto memoryDto = new CampaignChatMemoryDto();
        memoryDto.setId(campaignId);
        memoryDto.setTenantId(42L);

        stubDependencies();
        when(agentLeadCountAggregationService.count(any(), any(), any(), any(), any()))
                .thenReturn(countResult(4, 12, List.of("Corp X")));

        LeadFilterCriteria filter = LeadFilterCriteria.builder().companyStates(List.of("NY")).build();
        leadSearchAgentService.updateCampaignFiltersAndCount(memoryDto, filter);

        ArgumentCaptor<CampaignChatMemoryDto> captor = ArgumentCaptor.forClass(CampaignChatMemoryDto.class);
        verify(campaignChatMemoryService).updateCampaignChatMemory(captor.capture());

        TargetingCriteriaDto saved = captor.getValue().getTargetingCriteria();
        assertEquals(4, saved.getTotalCompanies());
        assertEquals(12, saved.getTotalContacts());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Stubs getCampaignChatMemoryById only.
     *
     * <p>Does NOT stub updateCampaignChatMemory — that is handled by
     * {@link #stubDependencies()} to avoid double-stubbing which causes
     * {@code UnnecessaryStubbingException}.
     */
    private void prepareMockCampaignMemory(Long campaignId) {
        CampaignChatMemoryDto memoryDto = new CampaignChatMemoryDto();
        memoryDto.setId(campaignId);
        memoryDto.setTenantId(1L);
        when(campaignChatMemoryService.getCampaignChatMemoryById(campaignId)).thenReturn(memoryDto);
    }

    /**
     * Builds a memory DTO with a pre-existing lead filter (for narrowDownContacts tests).
     * Does NOT stub any mock — callers must wire {@code getCampaignChatMemoryById} separately.
     */
    private CampaignChatMemoryDto buildMemoryWithFilter(Long campaignId, LeadFilterCriteria filter) {
        CampaignChatMemoryDto dto = new CampaignChatMemoryDto();
        dto.setId(campaignId);
        dto.setTenantId(1L);
        dto.setLeadFilter(filter);
        return dto;
    }

    private CampaignChatMemoryDto buildMemoryWithTargetingCriteria(Long campaignId,
                                                                   long companies,
                                                                   long contacts) {
        CampaignChatMemoryDto dto = new CampaignChatMemoryDto();
        dto.setId(campaignId);
        dto.setTenantId(1L);
        TargetingCriteriaDto criteria = new TargetingCriteriaDto();
        criteria.setTotalCompanies(companies);
        criteria.setTotalContacts(contacts);
        dto.setTargetingCriteria(criteria);
        return dto;
    }

    /**
     * Stubs all external service dependencies needed by the search/count path,
     * including {@code updateCampaignChatMemory}.
     * Call this instead of — not in addition to — stubbing updateCampaignChatMemory
     * in individual tests, to prevent {@code UnnecessaryStubbingException}.
     */
    private void stubDependencies() {
        when(leadDataPackService.getGatedInfo())
                .thenReturn(new GatedInfo(List.of(), false));
        when(vendorDataPackService.getVendorAccessForAuthenticatedUser(anyLong()))
                .thenReturn(Optional.of(new VendorAccess(List.of(), List.of(), false, null)));
        when(contactOutreachStatusService.getIneligibleContactIds(anyLong()))
                .thenReturn(Set.of());
        when(campaignChatMemoryService.updateCampaignChatMemory(any()))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    private AgentLeadCountResult emptyCountResult() {
        return AgentLeadCountResult.builder()
                .totalCompanies(0)
                .totalContacts(0)
                .sampleCompanyNames(List.of())
                .build();
    }

    private AgentLeadCountResult countResult(long companies, long contacts, List<String> samples) {
        return AgentLeadCountResult.builder()
                .totalCompanies(companies)
                .totalContacts(contacts)
                .sampleCompanyNames(samples)
                .build();
    }
}