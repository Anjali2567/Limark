package ai.leadplus.application.campaignagent.tools;

import ai.leadplus.application.campaignchatmemory.CampaignChatMemoryDto;
import ai.leadplus.application.campaignchatmemory.CampaignChatMemoryService;
import ai.leadplus.application.campaigngenerator.tools.LeadSearchToolResponse;
import ai.leadplus.application.common.LocationFilterDto;
import ai.leadplus.application.common.TargetingCriteriaDto;
import ai.leadplus.application.contactoutreachstatuses.ContactOutreachStatusService;
import ai.leadplus.application.leaddatapacks.GatedInfo;
import ai.leadplus.application.leaddatapacks.LeadDataPackService;
import ai.leadplus.application.leads.LeadFilterCriteria;
import ai.leadplus.application.vendordatapacks.VendorAccess;
import ai.leadplus.application.vendordatapacks.VendorDataPackService;
import ai.leadplus.domain.leadcompanies.EmployeeRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadSearchAgentService {

    private final CampaignChatMemoryService campaignChatMemoryService;
    private final ContactOutreachStatusService contactOutreachStatusService;
    private final VendorDataPackService vendorDataPackService;
    private final LeadDataPackService leadDataPackService;
    private final AgentLeadCountAggregationService agentLeadCountAggregationService;

    public LeadSearchToolResponse searchLeads(
            Long campaignId,
            List<String> companyNames,
            List<String> cities,
            List<String> states,
            List<String> countries,
            List<String> regions,
            List<String> keywords,
            List<String> industries,
            List<String> employeeCount,
            List<String> revenueRanges,
            List<String> technologies,
            List<String> toolsServices,
            List<String> titles,
            List<String> seniority,
            List<String> departments,
            List<String> postalCodes,
            List<String> sicCodes,
            List<String> naicsCodes
    ) {
        log.info(
                "Searching leads | campaignId: {} | companies: {} | contactLocations: [cities={}, states={}, countries={}] | " +
                        "regions: {} | industries: {} | titles: {} | departments: {} | employeeCount: {} | " +
                        "revenueRanges: {} | keywords: {} | technologies: {} | toolsServices: {} | " +
                        "seniority: {} | postalCodes: {} | sicCodes: {} | naicsCodes: {}",
                campaignId, companyNames, cities, states, countries,
                regions, industries, titles, departments, employeeCount, revenueRanges,
                keywords, technologies, toolsServices, seniority, postalCodes, sicCodes, naicsCodes
        );

        List<EmployeeRange> ranges = convertEmployeeRanges(employeeCount);

        LeadFilterCriteria leadFilterCriteria = LeadFilterCriteria.builder()
                .companyNames(companyNames)
                .cities(cities)
                .states(states)
                .countries(countries)
                .regions(regions)
                .keywords(keywords)
                .industries(industries)
                .employeeRanges(ranges)
                .revenueRanges(revenueRanges)
                .technologies(technologies)
                .toolsServices(toolsServices)
                .titles(titles)
                .seniority(seniority)
                .departments(departments)
                .postalCodes(postalCodes)
                .sicCodes(sicCodes)
                .naicsCodes(naicsCodes)
                .aggregateTechSearch(true)
                .build();

        CampaignChatMemoryDto campaignMemory = campaignChatMemoryService.getCampaignChatMemoryById(campaignId);
        Long tenantId = campaignMemory.getTenantId();

        GatedInfo gatedInfo = leadDataPackService.getGatedInfo();
        VendorAccess vendorAccess = vendorDataPackService
                .getVendorAccessForAuthenticatedUser(tenantId)
                .orElse(null);
        Set<Long> ineligibleContactIds =
                contactOutreachStatusService.getIneligibleContactIds(tenantId);

        AgentLeadCountResult result = agentLeadCountAggregationService.count(
                leadFilterCriteria, tenantId, gatedInfo, vendorAccess, ineligibleContactIds
        );

        saveFilterToMemory(campaignMemory, leadFilterCriteria, result);

        LeadSearchToolResponse.LeadSearchToolResponseBuilder response = LeadSearchToolResponse.builder()
                .totalCompanies(result.getTotalCompanies())
                .totalContacts(result.getTotalContacts())
                .sampleCompanyNames(result.getSampleCompanyNames());

        if (result.getTotalContacts() > 0) {
            response.searchResultId(String.valueOf(campaignMemory.getId()));
        }

        return response.build();
    }

    public LeadSearchToolResponse narrowDownContacts(
            Long campaignChatMemoryId,
            List<String> jobTitles,
            List<String> states,
            List<String> cities,
            List<String> countries
    ) {
        log.info("Executing narrowDownContacts | campaignChatMemoryId: {} | jobTitles: {} | states: {} | cities: {} | countries: {}",
                campaignChatMemoryId, jobTitles, states, cities, countries);

        CampaignChatMemoryDto campaignMemory =
                campaignChatMemoryService.getCampaignChatMemoryById(campaignChatMemoryId);

        LeadFilterCriteria baseFilter = campaignMemory.getLeadFilter() != null
                ? campaignMemory.getLeadFilter()
                : new LeadFilterCriteria();

        LeadFilterCriteria narrowedFilter = LeadFilterCriteria.builder()
                .companyNames(baseFilter.getCompanyNames())
                .companyCities(baseFilter.getCompanyCities())
                .companyStates(baseFilter.getCompanyStates())
                .companyCountries(baseFilter.getCompanyCountries())
                .regions(baseFilter.getRegions())
                .keywords(baseFilter.getKeywords())
                .industries(baseFilter.getIndustries())
                .employeeRanges(baseFilter.getEmployeeRanges())
                .revenueRanges(baseFilter.getRevenueRanges())
                .technologies(baseFilter.getTechnologies())
                .toolsServices(baseFilter.getToolsServices())
                .postalCodes(baseFilter.getPostalCodes())
                .sicCodes(baseFilter.getSicCodes())
                .naicsCodes(baseFilter.getNaicsCodes())
                .aggregateTechSearch(true)
                .seniority(baseFilter.getSeniority())
                .departments(baseFilter.getDepartments())
                .titles(jobTitles)
                .cities(cities)
                .states(states)
                .countries(countries)
                .build();

        GatedInfo gatedInfo = leadDataPackService.getGatedInfo();
        VendorAccess vendorAccess = vendorDataPackService
                .getVendorAccessForAuthenticatedUser(campaignMemory.getTenantId())
                .orElse(null);
        Set<Long> ineligibleContactIds =
                contactOutreachStatusService.getIneligibleContactIds(campaignMemory.getTenantId());

        AgentLeadCountResult result = agentLeadCountAggregationService.count(
                narrowedFilter,
                campaignMemory.getTenantId(),
                gatedInfo, vendorAccess, ineligibleContactIds
        );

        saveFilterToMemory(campaignMemory, narrowedFilter, result);

        return LeadSearchToolResponse.builder()
                .searchResultId(String.valueOf(campaignMemory.getId()))
                .totalCompanies(result.getTotalCompanies())
                .totalContacts(result.getTotalContacts())
                .sampleCompanyNames(result.getSampleCompanyNames())
                .build();
    }

    public LeadSearchToolResponse setContactLimit(Long campaignId, int limit) {
        log.info("setContactLimit | campaignId={} | limit={}", campaignId, limit);

        CampaignChatMemoryDto campaignMemory =
                campaignChatMemoryService.getCampaignChatMemoryById(campaignId);

        if (campaignMemory.getLeadFilter() == null) {
            return LeadSearchToolResponse.builder()
                    .totalCompanies(0)
                    .totalContacts(0)
                    .sampleCompanyNames(List.of())
                    .message("No search has been run yet. Please search for leads first, then set a limit.")
                    .build();
        }

        if (limit < 0) {
            return LeadSearchToolResponse.builder()
                    .totalCompanies(0)
                    .totalContacts(0)
                    .sampleCompanyNames(List.of())
                    .message("Contact limit must be a positive number.")
                    .build();
        }

        TargetingCriteriaDto targetingCriteria = campaignMemory.getTargetingCriteria();
        long totalCompanies = targetingCriteria != null ? targetingCriteria.getTotalCompanies() : 0;
        long totalContacts  = targetingCriteria != null ? targetingCriteria.getTotalContacts()  : 0;

        if (limit > 0 && limit >= totalContacts) {
            return LeadSearchToolResponse.builder()
                    .totalCompanies(totalCompanies)
                    .totalContacts(totalContacts)
                    .sampleCompanyNames(List.of())
                    .message("The limit you specified (" + limit + ") is equal to or greater than the " +
                            "current total of " + totalContacts + " contacts. No limit has been applied.")
                    .build();
        }

        campaignMemory.setContactLimit(limit == 0 ? null : limit);
        campaignChatMemoryService.updateCampaignChatMemory(campaignMemory);

        long effectiveContacts = (limit == 0) ? totalContacts : limit;

        return LeadSearchToolResponse.builder()
                .searchResultId(String.valueOf(campaignMemory.getId()))
                .totalCompanies(totalCompanies)
                .totalContacts(effectiveContacts)
                .sampleCompanyNames(List.of())
                .build();
    }

    public CampaignChatMemoryDto updateCampaignFiltersAndCount(CampaignChatMemoryDto campaignMemory,
                                                               LeadFilterCriteria leadFilterCriteria) {
        GatedInfo gatedInfo = leadDataPackService.getGatedInfo();
        VendorAccess vendorAccess = vendorDataPackService
                .getVendorAccessForAuthenticatedUser(campaignMemory.getTenantId())
                .orElse(null);
        Set<Long> ineligibleContactIds =
                contactOutreachStatusService.getIneligibleContactIds(campaignMemory.getTenantId());

        AgentLeadCountResult result = agentLeadCountAggregationService.count(
                leadFilterCriteria,
                campaignMemory.getTenantId(),
                gatedInfo, vendorAccess, ineligibleContactIds
        );

        log.info("updateCampaignFilters: campaignId={} | companies={} | contacts={}",
                campaignMemory.getId(), result.getTotalCompanies(), result.getTotalContacts());

        return saveFilterToMemory(campaignMemory, leadFilterCriteria, result);
    }

    private CampaignChatMemoryDto saveFilterToMemory(CampaignChatMemoryDto campaignMemory,
                                                     LeadFilterCriteria leadFilterCriteria,
                                                     AgentLeadCountResult result) {
        TargetingCriteriaDto targetingCriteria = campaignMemory.getTargetingCriteria();
        if (targetingCriteria == null) {
            targetingCriteria = new TargetingCriteriaDto();
        }

        targetingCriteria.setCompanyIds(null);
        targetingCriteria.setContactIds(null);
        targetingCriteria.setTotalCompanies(result.getTotalCompanies());
        targetingCriteria.setTotalContacts(result.getTotalContacts());

        campaignMemory.setContactLimit(null);

        targetingCriteria.setCompanyLocationFilter(buildLocationFilter(
                leadFilterCriteria.getCompanyCities(),
                leadFilterCriteria.getCompanyStates(),
                leadFilterCriteria.getCompanyCountries(),
                leadFilterCriteria.getRegions()));
        targetingCriteria.setContactLocationFilter(buildLocationFilter(
                leadFilterCriteria.getCities(),
                leadFilterCriteria.getStates(),
                leadFilterCriteria.getCountries(),
                null));
        targetingCriteria.setJobTitles(leadFilterCriteria.getTitles());
        targetingCriteria.setEmployeeRanges(leadFilterCriteria.getEmployeeRanges());

        campaignMemory.setTargetingCriteria(targetingCriteria);
        campaignMemory.setLeadFilter(leadFilterCriteria);
        campaignMemory.setLastSearchAt(LocalDateTime.now());

        return campaignChatMemoryService.updateCampaignChatMemory(campaignMemory);
    }

    private List<EmployeeRange> convertEmployeeRanges(List<String> employeeCount) {
        return Optional.ofNullable(employeeCount)
                .orElse(Collections.emptyList())
                .stream()
                .map(EmployeeRange::fromString)
                .toList();
    }

    private LocationFilterDto buildLocationFilter(List<String> cities, List<String> states,
                                                  List<String> countries, List<String> regions) {
        if (CollectionUtils.isEmpty(cities) && CollectionUtils.isEmpty(states)
                && CollectionUtils.isEmpty(countries) && CollectionUtils.isEmpty(regions)) {
            return null;
        }
        return LocationFilterDto.builder()
                .cities(cities)
                .states(states)
                .countries(countries)
                .regions(regions)
                .build();
    }
}