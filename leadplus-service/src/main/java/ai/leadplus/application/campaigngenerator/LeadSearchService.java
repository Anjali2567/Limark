package ai.leadplus.application.campaigngenerator;

import ai.leadplus.application.campaigncontacts.CampaignContactService;
import ai.leadplus.application.campaigngenerator.tools.LeadSearchToolResponse;
import ai.leadplus.application.campaigns.CampaignDto;
import ai.leadplus.application.campaigns.CampaignService;
import ai.leadplus.application.common.LocationFilterDto;
import ai.leadplus.application.common.TargetingCriteriaDto;
import ai.leadplus.application.contactoutreachstatuses.ContactOutreachStatusService;
import ai.leadplus.application.leadcompany.LeadCompanySearchService;
import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.application.leadcontact.LeadContactSearchService;
import ai.leadplus.application.leadcontact.LeadContactService;
import ai.leadplus.application.leaddatapacks.GatedInfo;
import ai.leadplus.application.leaddatapacks.LeadDataPackService;
import ai.leadplus.application.leads.ContactLeadSearchService;
import ai.leadplus.application.leads.LeadFilterCriteria;
import ai.leadplus.application.vendordatapacks.VendorAccess;
import ai.leadplus.application.vendordatapacks.VendorDataPackService;
import ai.leadplus.domain.leadcompanies.EmployeeRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadSearchService {

    private static final double SIMILARITY_THRESHOLD = 0.62;

    private final CampaignService campaignService;
    private final LeadContactService leadContactService;
    private final LeadCompanySearchService leadCompanySearchService;
    private final LeadContactSearchService leadContactSearchService;
    private final CampaignContactService campaignContactService;
    private final ContactOutreachStatusService contactOutreachStatusService;
    private final ContactLeadSearchService contactLeadSearchService;
    private final VendorDataPackService vendorDataPackService;
    private final LeadDataPackService leadDataPackService;

    public LeadSearchToolResponse performGenericSearch(
            String campaignId,
            List<String> companyNames,
            List<String> employeeCount,
            List<String> states,
            List<String> cities,
            List<String> countries,
            List<String> regions,
            boolean excludeLocations
    ) {
        logSearchExecution("genericSearch", companyNames, employeeCount, states, cities, countries, regions, excludeLocations);

        List<EmployeeRange> ranges = convertEmployeeRanges(employeeCount);
        List<Long> companyIds;

        if (excludeLocations) {
            companyIds = leadCompanySearchService.searchLeadCompaniesWithExclusion(
                            null, ranges, states, cities, countries, regions
                    ).stream()
                    .map(company -> company.getId())
                    .toList();
        } else {
            companyIds = leadCompanySearchService.searchLeadCompanies(companyNames, null, ranges, states, cities, countries, regions)
                    .stream()
                    .map(company -> company.getId())
                    .toList();
        }

        LocationFilterDto locationFilterDto = buildLocationFilter(states, cities, countries, regions);

        if (excludeLocations) {
            return updateCampaign(campaignId, null, ranges, companyIds, null, locationFilterDto);
        } else {
            return updateCampaign(campaignId, null, ranges, companyIds, locationFilterDto, null);
        }
    }

    public LeadSearchToolResponse narrowDownContacts(
            String campaignId,
            List<String> jobTitles,
            List<String> states,
            List<String> cities,
            List<String> countries
    ) {
        log.info("Executing narrowDownContacts with campaignId: {}, jobTitles: {}, states: {}, cities: {}, countries: {}",
                campaignId, jobTitles, states, cities, countries);

        CampaignDto campaign = campaignService.getCampaignById(Long.parseLong(campaignId));
        List<Long> companyIds = campaign.getTargetingCriteria().getCompanyIds();

        if (companyIds == null || companyIds.isEmpty()) {
            log.warn("No companies found in campaign: {}", campaignId);
            return LeadSearchToolResponse.builder()
                    .searchResultId(campaignId)
                    .totalCompanies(0)
                    .totalContacts(0)
                    .build();
        }

        List<LeadContactDto> filteredContacts = leadContactSearchService.searchContactsByCompaniesAndFilters(
                companyIds, jobTitles, states, cities, countries
        ).stream()
                .map(LeadContactDto::toDto)
                .toList();
        List<LeadContactDto> eligibleContacts = contactOutreachStatusService.filterCampaignEligibleContacts(campaign.getTenantId(), filteredContacts);

        List<Long> filteredContactIds = eligibleContacts.stream()
                .map(LeadContactDto::getId)
                .filter(Objects::nonNull)
                .toList();
        List<Long> filteredCompanyIds = eligibleContacts.stream()
                .map(LeadContactDto::getLeadCompanyId)
                .distinct()
                .toList();

        LeadSearchToolResponse response = updateCampaignWithContacts(
                campaign,
                jobTitles,
                filteredCompanyIds,
                filteredContactIds,
                states,
                cities,
                countries
        );
        response.setTotalCompanies(filteredCompanyIds.size());
        return response;
    }

    private List<EmployeeRange> convertEmployeeRanges(List<String> employeeCount) {
        return Optional.ofNullable(employeeCount)
                .orElse(Collections.emptyList())
                .stream()
                .map(EmployeeRange::fromString)
                .toList();
    }

    private LocationFilterDto buildLocationFilter(List<String> states, List<String> cities,
                                                  List<String> countries, List<String> regions) {
        boolean hasAny = (states != null && !states.isEmpty())
                || (cities != null && !cities.isEmpty())
                || (countries != null && !countries.isEmpty())
                || (regions != null && !regions.isEmpty());
        if (!hasAny) {
            return null;
        }
        return LocationFilterDto.builder()
                .states(states)
                .cities(cities)
                .countries(countries)
                .regions(regions)
                .build();
    }

    private void logSearchExecution(
            String methodName,
            List<String> companyNames,
            List<String> employeeCount,
            List<String> states,
            List<String> cities,
            List<String> countries,
            List<String> regions,
            boolean excludeLocations) {
        String prefix = excludeLocations ? "exclude" : "";
        log.info("Executing {} with filters -> companyNames: [{}], employeeCount: [{}], {}states: [{}], {}cities: [{}], {}countries: [{}], {}regions: [{}]",
                methodName,
                companyNames,
                employeeCount,
                prefix,
                String.join(", ", safeGetList(states)),
                prefix,
                String.join(", ", safeGetList(cities)),
                prefix,
                String.join(", ", safeGetList(countries)),
                prefix,
                String.join(", ", safeGetList(regions)));
    }

    private List<String> safeGetList(List<String> list) {
        return Optional.ofNullable(list).orElse(Collections.emptyList());
    }

    private List<Long> applyGatingForTenant(List<Long> companyIds, Long tenantId) {
        if (companyIds == null || companyIds.isEmpty()) return companyIds;
        GatedInfo gatedInfo = leadDataPackService.getGatedInfo();
        boolean gatingActive = gatedInfo != null
                && (!CollectionUtils.isEmpty(gatedInfo.getNamedGatedSegments()) || gatedInfo.isNullGated());

        Optional<VendorAccess> accessOpt = vendorDataPackService.getVendorAccessForAuthenticatedUser(tenantId);
        if (accessOpt.isEmpty()) {
            return gatingActive ? List.of() : companyIds;
        }
        return leadCompanySearchService.filterCompanyIdsByGate(companyIds, gatedInfo, accessOpt.get());
    }

    private LeadSearchToolResponse updateCampaign(String campaignId,
                                                  String searchQuery,
                                                  List<EmployeeRange> employeeRanges,
                                                  List<Long> companyIds,
                                                  LocationFilterDto locationFilterDto,
                                                  LocationFilterDto excludeLocationFilterDto) {
        CampaignDto campaignDto = campaignService.getCampaignById(Long.parseLong(campaignId));
        TargetingCriteriaDto targetingCriteria = campaignDto.getTargetingCriteria();
        if (targetingCriteria == null) {
            targetingCriteria = new TargetingCriteriaDto();
        }

        targetingCriteria.setCompanyIds(companyIds);
        targetingCriteria.setEmployeeRanges(employeeRanges);
        targetingCriteria.setCompanyExcludeLocationFilter(excludeLocationFilterDto);
        targetingCriteria.setCompanyLocationFilter(locationFilterDto);
        targetingCriteria.setSearchQuery(searchQuery);
        targetingCriteria.setJobTitles(null);
        targetingCriteria.setContactLocationFilter(null);

        List<LeadContactDto> leadContacts = leadContactService.getByLeadCompanyIds(companyIds);
        List<LeadContactDto> eligibleContacts = contactOutreachStatusService.filterCampaignEligibleContacts(campaignDto.getTenantId(), leadContacts);
        List<Long> eligibleCompanyIds = eligibleContacts.stream()
                .map(LeadContactDto::getLeadCompanyId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Set<Long> leadContactIds = eligibleContacts.stream()
                .map(LeadContactDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        targetingCriteria.setCompanyIds(eligibleCompanyIds);

        targetingCriteria.setContactIds(new java.util.ArrayList<>(leadContactIds));
        campaignDto.setTargetingCriteria(targetingCriteria);
        campaignContactService.updateContactIdsOfCampaign(campaignDto.getTenantId(), campaignDto.getId(), leadContactIds, false);
        int totalContacts = leadContactIds.size();
        CampaignDto savedCampaignDto = campaignService.updateCampaign(campaignDto);

        return LeadSearchToolResponse.builder()
                .searchResultId(String.valueOf(savedCampaignDto.getId()))
                .totalCompanies(eligibleCompanyIds.size())
                .totalContacts(totalContacts)
                .build();
    }

    private LeadSearchToolResponse updateCampaignWithContacts(CampaignDto campaign,
                                                              List<String> jobTitles,
                                                              List<Long> filteredCompanyIds,
                                                              List<Long> filteredContactIds,
                                                              List<String> states, List<String> cities,
                                                              List<String> countries) {
        TargetingCriteriaDto targetingCriteria = campaign.getTargetingCriteria();
        if (targetingCriteria == null) {
            targetingCriteria = new TargetingCriteriaDto();
        }
        if (states != null || cities != null || countries != null) {
            LocationFilterDto locationFilter = LocationFilterDto.builder()
                    .states(states)
                    .cities(cities)
                    .countries(countries)
                    .build();
            targetingCriteria.setContactLocationFilter(locationFilter);
        }

        List<Long> companyIds = Optional.ofNullable(filteredCompanyIds)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<Long> contactIds = Optional.ofNullable(filteredContactIds)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .toList();

        targetingCriteria.setJobTitles(jobTitles);
        targetingCriteria.setCompanyIds(companyIds);
        targetingCriteria.setContactIds(contactIds);
        campaignContactService.updateContactIdsOfCampaign(campaign.getTenantId(), campaign.getId(), new java.util.HashSet<>(contactIds), false);
        campaign.setTargetingCriteria(targetingCriteria);

        CampaignDto savedCampaign = campaignService.updateCampaign(campaign);

        return LeadSearchToolResponse.builder()
                .searchResultId(String.valueOf(savedCampaign.getId()))
                .totalCompanies(companyIds.size())
                .totalContacts(contactIds.size())
                .build();
    }

    public LeadSearchToolResponse searchLeads(
            String campaignId,
            List<String> companyNames,
            List<String> companyCities,
            List<String> companyStates,
            List<String> companyCountries,
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
                "searchLeads | campaignId: {} | companyNames: {} | companyCities: {} | companyStates: {} | " +
                        "companyCountries: {} | regions: {} | keywords: {} | industries: {} | employeeCount: {} | " +
                        "revenueRanges: {} | technologies: {} | toolsServices: {} | titles: {} | seniority: {} | " +
                        "departments: {} | postalCodes: {} | sicCodes: {} | naicsCodes: {}",
                campaignId, companyNames, companyCities, companyStates, companyCountries, regions, keywords,
                industries, employeeCount, revenueRanges, technologies, toolsServices, titles, seniority,
                departments, postalCodes, sicCodes, naicsCodes
        );
        List<EmployeeRange> ranges = convertEmployeeRanges(employeeCount);

        LeadFilterCriteria leadFilterCriteria = LeadFilterCriteria.builder()
                .companyNames(companyNames)
                .companyCities(companyCities)
                .companyStates(companyStates)
                .companyCountries(companyCountries)
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

        CampaignDto campaignDto = campaignService.getCampaignById(Long.parseLong(campaignId));
        GatedInfo gatedInfo = leadDataPackService.getGatedInfo();
        VendorAccess vendorAccess = vendorDataPackService
                .getVendorAccessForAuthenticatedUser(campaignDto.getTenantId())
                .orElse(null);
        List<Long> matchedContactIds = contactLeadSearchService.findContactIds(
                leadFilterCriteria,
                null,
                null,
                campaignDto.getTenantId(),
                gatedInfo,
                vendorAccess
        );
        return updateCampaignFromContactIds(campaignId, leadFilterCriteria, matchedContactIds);
    }

    private LeadSearchToolResponse updateCampaignFromContactIds(String campaignId, LeadFilterCriteria leadFilterCriteria, List<Long> contactIds) {
        CampaignDto campaignDto = campaignService.getCampaignById(Long.parseLong(campaignId));
        List<Long> candidateContactIds = Optional.ofNullable(contactIds)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .toList();
        Set<Long> ineligibleContactIds = contactOutreachStatusService.getIneligibleContactIds(
                campaignDto.getTenantId(),
                candidateContactIds
        );
        List<Long> eligibleCandidateContactIds = candidateContactIds.stream()
                .filter(id -> !ineligibleContactIds.contains(id))
                .toList();

        List<LeadContactDto> matchedContacts = leadContactService.getLeadContactsByIds(
                eligibleCandidateContactIds
        );

        List<Long> allCompanyIds = matchedContacts.stream()
                .map(LeadContactDto::getLeadCompanyId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<Long> accessibleCompanyIds = applyGatingForTenant(allCompanyIds, campaignDto.getTenantId());
        Set<Long> accessibleSet = new HashSet<>(accessibleCompanyIds);
        List<LeadContactDto> gatedContacts = matchedContacts.stream()
                .filter(c -> c.getLeadCompanyId() != null && accessibleSet.contains(c.getLeadCompanyId()))
                .toList();

        List<LeadContactDto> eligibleContacts = gatedContacts;
        List<Long> eligibleCompanyIds = eligibleContacts.stream()
                .map(LeadContactDto::getLeadCompanyId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Set<Long> eligibleContactIds = eligibleContacts.stream()
                .map(LeadContactDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        TargetingCriteriaDto targetingCriteria = campaignDto.getTargetingCriteria();
        if (targetingCriteria == null) {
            targetingCriteria = new TargetingCriteriaDto();
        }
        targetingCriteria.setCompanyIds(eligibleCompanyIds);
        targetingCriteria.setContactIds(new java.util.ArrayList<>(eligibleContactIds));
        targetingCriteria.setEmployeeRanges(leadFilterCriteria.getEmployeeRanges());
        targetingCriteria.setCompanyLocationFilter(buildLocationFilter(
                leadFilterCriteria.getCompanyStates(),
                leadFilterCriteria.getCompanyCities(),
                leadFilterCriteria.getCompanyCountries(),
                leadFilterCriteria.getRegions()));
        targetingCriteria.setSearchQuery(null);
        targetingCriteria.setCompanyExcludeLocationFilter(null);
        targetingCriteria.setJobTitles(null);
        targetingCriteria.setContactLocationFilter(null);

        campaignDto.setTargetingCriteria(targetingCriteria);
        campaignContactService.updateContactIdsOfCampaign(campaignDto.getTenantId(), campaignDto.getId(), eligibleContactIds, false);
        CampaignDto savedCampaign = campaignService.updateCampaign(campaignDto);

        return LeadSearchToolResponse.builder()
                .searchResultId(String.valueOf(savedCampaign.getId()))
                .totalCompanies(eligibleCompanyIds.size())
                .totalContacts(eligibleContactIds.size())
                .build();
    }
}
