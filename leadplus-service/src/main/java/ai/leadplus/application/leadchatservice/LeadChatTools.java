package ai.leadplus.application.leadchatservice;

import ai.leadplus.application.leadqueries.LeadQueryService;
import ai.leadplus.application.leadcompany.LeadCompanyWithContactCountDto;
import ai.leadplus.application.leads.LeadFilterCriteria;
import ai.leadplus.application.leads.LeadDto;
import ai.leadplus.application.leads.TenantLeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadChatTools {

    private static final Pageable PREVIEW_PAGE = PageRequest.of(0, 5);
    private static final Pageable COUNT_PAGE = PageRequest.of(0, 1);

    private final LeadQueryService leadQueryService;
    private final TenantLeadService tenantLeadService;

    @Tool(
            name = "searchLeadSeniority",
            description = """
                            Searches for available seniority levels matching the given text.
                            Returns a list of matching seniority values."""
    )
    public LeadFilterCriteria searchLeadSeniority(
            @ToolParam(
                    description = "List of lead seniority levels to search for",
                    required = true
            ) List<String> seniorityList) {

        log.info("Searching for lead attributes - Seniority: {}", seniorityList);
        return leadQueryService.mapToLeadFilterCriteria(null, seniorityList);
    }

    @Tool(
            name = "previewLeadResults",
            description = """
                    Reads the first page of tenant lead results for the provided criteria.
                    Returns whether data exists, total hits, and sample records.
                    Use this before finalizing response wording when filters are applied."""
    )
    public LeadResultPreview previewLeadResults(
            @ToolParam(description = "Tenant id for scoped lead search", required = true) Long tenantId,
            @ToolParam(description = "Parsed lead filter criteria to validate against first page results", required = true) LeadFilterCriteria criteria) {

        if (criteria == null || !criteria.hasAnyFilters()) {
            return LeadResultPreview.builder()
                    .scope("none")
                    .hasResults(false)
                    .totalResults(0)
                    .samples(List.of())
                    .feedback("No active filters to preview.")
                    .build();
        }

        if (criteria.hasContactFilters()) {
            Page<LeadDto> contactPage = tenantLeadService.searchContacts(
                    tenantId,
                    criteria,
                    null,
                    criteria.getCompanyIds(),
                    PREVIEW_PAGE
            );
            List<String> contactSamples = contactPage.getContent().stream()
                    .map(this::toContactSample)
                    .toList();

            if (contactPage.hasContent()) {
                return LeadResultPreview.builder()
                        .scope("contacts")
                        .hasResults(true)
                        .totalResults(contactPage.getTotalElements())
                        .samples(contactSamples)
                        .feedback("Contacts found for current filters.")
                        .build();
            }
        }

        Page<LeadCompanyWithContactCountDto> companyPage = tenantLeadService.searchCompanies(
                tenantId,
                criteria,
                null,
                PREVIEW_PAGE
        );
        List<String> companySamples = companyPage.getContent().stream()
                .map(this::toCompanySample)
                .toList();

        if (companyPage.hasContent()) {
            return LeadResultPreview.builder()
                    .scope("companies")
                    .hasResults(true)
                    .totalResults(companyPage.getTotalElements())
                    .samples(companySamples)
                    .feedback("Companies found for current filters.")
                    .build();
        }

        return LeadResultPreview.builder()
                .scope(criteria.hasContactFilters() ? "contacts" : "companies")
                .hasResults(false)
                .totalResults(0)
                .samples(List.of())
                .feedback("No first-page results for current filters. Consider broadening industries, location, titles, or seniority.")
                .build();
    }

    @Tool(
            name = "countCurrentAudience",
            description = """
                    Counts the current lead audience for the provided criteria.
                    Returns exact company count, exact contact count, and sample company names.
                    Call this when the user asks about the current audience, audience size,
                    current targeting, or how many companies/contacts match the active filters."""
    )
    public LeadAudienceCount countCurrentAudience(
            @ToolParam(description = "Tenant id for scoped lead counting", required = true) Long tenantId,
            @ToolParam(description = "Current selected lead filter criteria. Use an empty object when no criteria are selected.", required = false) LeadFilterCriteria criteria) {

        LeadFilterCriteria activeCriteria = criteria == null ? new LeadFilterCriteria() : criteria;

        Page<LeadDto> contactPage = tenantLeadService.searchContacts(
                tenantId,
                activeCriteria,
                null,
                activeCriteria.getCompanyIds(),
                COUNT_PAGE
        );
        boolean hasContactFilters = activeCriteria.hasContactFilters();

        long totalCompanies;
        List<String> sampleCompanyNames;

        if (hasContactFilters) {
            totalCompanies = tenantLeadService.countMatchingCompanies(
                    tenantId,
                    activeCriteria,
                    null,
                    activeCriteria.getCompanyIds()
            );
            sampleCompanyNames = contactPage.getContent().stream()
                    .map(LeadDto::getCompanyName)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .limit(5)
                    .toList();
        } else {
            Page<LeadCompanyWithContactCountDto> companyPage = tenantLeadService.searchCompanies(
                    tenantId,
                    activeCriteria,
                    null,
                    PREVIEW_PAGE
            );
            totalCompanies = companyPage.getTotalElements();
            sampleCompanyNames = companyPage.getContent().stream()
                    .map(this::toCompanySample)
                    .toList();
        }

        return LeadAudienceCount.builder()
                .totalCompanies(totalCompanies)
                .totalContacts(contactPage.getTotalElements())
                .sampleCompanyNames(sampleCompanyNames)
                .feedback("Counts reflect the current selected criteria.")
                .build();
    }

    private String toContactSample(LeadDto contact) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(contact.getFirstName()) || StringUtils.hasText(contact.getLastName())) {
            builder.append((contact.getFirstName() == null ? "" : contact.getFirstName()).trim());
            if (StringUtils.hasText(contact.getLastName())) {
                if (builder.length() > 0) {
                    builder.append(" ");
                }
                builder.append(contact.getLastName().trim());
            }
        } else {
            builder.append("Contact");
        }

        if (StringUtils.hasText(contact.getTitle())) {
            builder.append(" - ").append(contact.getTitle());
        }
        if (StringUtils.hasText(contact.getCompanyName())) {
            builder.append(" @ ").append(contact.getCompanyName());
        }
        return builder.toString();
    }

    private String toCompanySample(LeadCompanyWithContactCountDto company) {
        String companyName = StringUtils.hasText(company.getName()) ? company.getName() : "Company";
        if (StringUtils.hasText(company.getDomain())) {
            return companyName + " (" + company.getDomain() + ")";
        }
        return companyName;
    }
}
