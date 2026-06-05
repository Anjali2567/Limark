package ai.leadplus.application.campaignagent.tools;

import ai.leadplus.application.campaignchatmemory.CampaignChatMemoryService;
import ai.leadplus.application.campaigngenerator.tools.LeadSearchToolResponse;
import ai.leadplus.application.leadcompany.LeadCompanyService;
import ai.leadplus.application.vendordatapacks.VendorAccess;
import ai.leadplus.application.vendordatapacks.VendorDataPackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeadSearchAgentTools {

    private final LeadSearchAgentService leadSearchAgentService;
    private final LeadCompanyService leadCompanyService;
    private final CampaignChatMemoryService campaignChatMemoryService;
    private final VendorDataPackService vendorDataPackService;

    @Tool(
            name = "countLeads",
            description = """
                    Count campaign-ready leads matching the given filter criteria.
                    Returns exact company and contact counts plus sample company names.
                    
                    WHEN TO CALL:
                    Call whenever the user wants to apply, add, update, or confirm any targeting filter.
                    Always call immediately after getIndustryList — never leave getIndustryList as the final action.
                    
                    CRITICAL — techTerms vs companyIndustries:
                    
                    techTerms: Use for ALL technology domains, methodologies, and descriptors:
                      AI, ML, machine learning, artificial intelligence, deep learning, NLP, fintech,
                      SaaS, cloud, IoT, blockchain, healthtech, edtech, cybersecurity, robotics software,
                      autonomous vehicles, data analytics, digital transformation, e-commerce, etc.
                      Rule: if the user says "X startups" or "X companies" where X is a technology
                      or domain term, put X in techTerms. Example: "AI/ML startups" →
                      techTerms: ["AI", "Machine Learning", "Artificial Intelligence"]
                    
                    companyIndustries: Use ONLY for named segments from getIndustryList.
                      These are specific business segments like "Robotics", "Medical",
                      "Food Equipment", "Logistics" — NOT technology domain names.
                      If the user does not explicitly name a segment from getIndustryList,
                      leave companyIndustries empty and use techTerms instead.
                    
                    FILTER BEHAVIOUR:
                    - All parameters are optional. Omitted parameters are not applied.
                    - To ADD: re-send existing filters plus new ones.
                    - To RESET: send only the new filters.
                    """
    )
    public LeadSearchToolResponse countLeads(
            @ToolParam(description = "Id of the current campaign") Long campaignId,
            @ToolParam(description = "Company names (e.g., 'Siemens', 'Bosch')", required = false)
            List<String> companyNames,
            @ToolParam(description = "State or province names in full — never abbreviate (e.g., 'California', 'Bavaria'). Filters by contact location.", required = false)
            List<String> states,
            @ToolParam(description = "City names in full (e.g., 'Los Angeles', 'Munich'). Filters by contact location.", required = false)
            List<String> cities,
            @ToolParam(description = "Country names in full — never abbreviate (e.g., 'Germany' not 'DE', 'United States' not 'US'). Filters by contact location.", required = false)
            List<String> countries,
            @ToolParam(description = "Broad sales regions in CamelCase (e.g., 'Europe', 'EastCoast', 'DACH', 'APAC').", required = false)
            List<String> companyRegions,
            @ToolParam(description = "Postal codes (e.g., '90001', '10001')", required = false)
            List<String> companyPostalCodes,
            @ToolParam(description = "SIC codes (e.g., '7372', '5812')", required = false)
            List<String> companySicCodes,
            @ToolParam(description = "NAICS codes (e.g., '541511', '523930')", required = false)
            List<String> companyNaicsCodes,
            @ToolParam(description = """
                    Technologies, platforms, tools, domain terms, AND technology-industry descriptors.
                    Use for everything technology-related:
                    - Tech stacks: Python, AWS, Kubernetes, SAP, Salesforce
                    - Domains: AI, machine learning, artificial intelligence, deep learning, NLP
                    - Industry descriptors: fintech, SaaS, healthtech, edtech, autonomous vehicles
                    - Methodologies: agile, DevOps, digital transformation
                    "AI/ML startups" → ["AI", "Machine Learning", "Artificial Intelligence"]
                    "fintech companies" → ["fintech", "financial technology"]
                    """, required = false)
            List<String> techTerms,
            @ToolParam(description = """
                    Business segment — ONLY use values returned by getIndustryList (e.g. "Robotics", "Medical", "Food Equipment", "Logistics").
                    DO NOT use technology domains here (AI, ML, fintech, SaaS, cloud).
                    Leave empty if the user describes a technology domain rather than a named business segment.
                    """, required = false)
            List<String> companyIndustries,
            @ToolParam(description = "Employee count ranges: '0-500', '501-1000', '1001-5000', '5001-10000', '10001+'", required = false)
            List<String> employeeCount,
            @ToolParam(description = "Annual revenue ranges in USD (e.g., '0-1000000', '10000000+')", required = false)
            List<String> revenueRanges,
            @ToolParam(description = """
                    Job titles or functional areas. Uses contains matching — partial terms work.
                    Examples: 'CEO', 'Sales Director', 'VP of Engineering', 'Supply Chain', 'Procurement'.
                    'Supply Chain' will match 'Supply Chain Manager', 'VP of Supply Chain', etc.
                    'Procurement' will match 'Procurement Specialist', 'Procurement Manager', etc.
                    Always use titles for role-related terms — never split into department + seniority.
                    """, required = false)
            List<String> titles,
            @ToolParam(description = """
                    Standalone seniority only when no function is named: 'C-Suite', 'VP', 'Director', 'Manager'.
                    Do NOT use when a function is mentioned ('Sales Directors' → titles, not seniority).
                    """, required = false)
            List<String> seniority,
            @ToolParam(description = """
                    Broad department — only when user wants everyone in a function with no title/level.
                    Examples: 'Engineering', 'Marketing', 'Finance'.
                    'Sales Directors' → titles, not departments.
                    """, required = false)
            List<String> departments
    ) {
        log.info("[TOOL] countLeads called for campaignId={}", campaignId);

        List<String> validatedIndustries = null;

        if (!CollectionUtils.isEmpty(companyIndustries)) {

            Set<String> actualSegments = new HashSet<>(leadCompanyService.getUniqueSegments());
            Set<String> allKnownValues = actualSegments.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            List<String> systemValidated = companyIndustries.stream()
                    .filter(i -> StringUtils.hasText(i) && allKnownValues.contains(i.toLowerCase()))
                    .toList();

            List<String> unknownValues = companyIndustries.stream()
                    .filter(i -> StringUtils.hasText(i) && !allKnownValues.contains(i.toLowerCase()))
                    .toList();

            if (!unknownValues.isEmpty()) {
                String unknown   = String.join(", ", unknownValues);
                String available = String.join(", ", actualSegments);
                log.warn("[TOOL] countLeads — unrecognised segment value(s): [{}]", unknown);
                return LeadSearchToolResponse.builder()
                        .totalCompanies(0)
                        .totalContacts(0)
                        .sampleCompanyNames(List.of())
                        .message("The following segment(s) are not recognised in the database: ["
                                + unknown + "]. Available segments are: [" + available + "]. "
                                + "Note: technology domain terms like AI, ML, fintech, SaaS belong "
                                + "in techTerms, not companyIndustries. "
                                + "Please call countLeads again with only valid segment values.")
                        .build();
            }

            if (systemValidated.isEmpty()) {
                validatedIndustries = null;
            } else {
                Optional<Set<String>> allowedOpt = getAccessibleSegments(campaignId);
                if (allowedOpt.isPresent()) {
                    Set<String> allowed = allowedOpt.get();

                    List<String> licensed = systemValidated.stream()
                            .filter(i -> allowed.stream().anyMatch(a -> a.equalsIgnoreCase(i)))
                            .toList();


                    List<String> denied = systemValidated.stream()
                            .filter(i -> licensed.stream().noneMatch(l -> l.equalsIgnoreCase(i)))
                            .toList();

                    if (!denied.isEmpty()) {
                        log.warn("[TOOL] countLeads — segment(s) not licensed: [{}]", denied);
                        return LeadSearchToolResponse.builder()
                                .totalCompanies(0)
                                .totalContacts(0)
                                .sampleCompanyNames(List.of())
                                .message("Your account is not licensed for the following segment(s): ["
                                        + String.join(", ", denied) + "]. "
                                        + "Your accessible segments are: ["
                                        + String.join(", ", allowed) + "]. "
                                        + "Please call countLeads again using only your licensed segments.")
                                .build();
                    }

                    validatedIndustries = licensed;
                } else {
                    validatedIndustries = systemValidated;
                }
            }
        }

        return leadSearchAgentService.searchLeads(
                campaignId,
                companyNames,
                cities,
                states,
                countries,
                companyRegions,
                techTerms,
                validatedIndustries,
                employeeCount,
                revenueRanges,
                null,
                null,
                titles,
                seniority,
                // departments intentionally not passed — causes misrouting of terms like "Supply Chain"
                null,
                companyPostalCodes,
                companySicCodes,
                companyNaicsCodes
        );
    }

    @Tool(
            name = "getIndustryList",
            description = """
                    Returns the available business segments in the system.
                    The list is already filtered to your account's licensed segments.
                    
                    WHEN TO CALL:
                    - When the user asks what segments/industries they can target.
                    - When countLeads returns a message about an unrecognised segment.
                    
                    IMPORTANT: This is a lookup tool only.
                    After calling this you MUST immediately call countLeads.
                    Never respond to the user after this tool without first calling countLeads.
                    
                    NOTE: Technology domain terms (AI, ML, fintech, SaaS, cloud, IoT) will NOT appear
                    in this list — they are not business segments. Use them in techTerms in countLeads.
                    """
    )
    public List<String> getIndustryList(
            @ToolParam(description = "Id of the current campaign") Long campaignId
    ) {
        log.info("[TOOL] getIndustryList called for campaignId={}", campaignId);

        List<String> actualSegments = leadCompanyService.getUniqueSegments();

        Optional<Set<String>> allowedOpt = getAccessibleSegments(campaignId);
        if (allowedOpt.isEmpty()) {
            return actualSegments;
        }

        Set<String> allowed = allowedOpt.get();
        return actualSegments.stream()
                .filter(seg -> allowed.stream().anyMatch(a -> a.equalsIgnoreCase(seg)))
                .toList();
    }

    @Tool(
            name = "setContactLimit",
            description = """
                Set a maximum number of contacts to include in this campaign.
                Call when user says: "limit to X", "cut down to X", "cap it at X", "I only want X".
                Only call AFTER countLeads has returned results. Does not re-run the search.
                Pass limit = 0 to remove a previously set limit.
                """
    )
    public LeadSearchToolResponse setContactLimit(
            @ToolParam(description = "Id of the current campaign") Long campaignId,
            @ToolParam(description = "Maximum contacts to include. Pass 0 to remove the limit.") int limit
    ) {
        log.info("[TOOL] setContactLimit called | campaignId={} | limit={}", campaignId, limit);
        return leadSearchAgentService.setContactLimit(campaignId, limit);
    }

    // -------------------------------------------------------------------------
    // Private helper
    // -------------------------------------------------------------------------

    private Optional<Set<String>> getAccessibleSegments(Long campaignId) {
        Long tenantId = campaignChatMemoryService.getCampaignChatMemoryById(campaignId).getTenantId();
        Optional<VendorAccess> accessOpt = vendorDataPackService.getVendorAccessForAuthenticatedUser(tenantId);
        if (accessOpt.isEmpty()
                || (CollectionUtils.isEmpty(accessOpt.get().getNamedVendorSegments())
                && CollectionUtils.isEmpty(accessOpt.get().getAccessibleIndustryNames()))) {
            return Optional.empty();
        }
        Set<String> allowed = new HashSet<>();
        if (!CollectionUtils.isEmpty(accessOpt.get().getNamedVendorSegments())) {
            allowed.addAll(accessOpt.get().getNamedVendorSegments());
        }
        if (!CollectionUtils.isEmpty(accessOpt.get().getAccessibleIndustryNames())) {
            allowed.addAll(accessOpt.get().getAccessibleIndustryNames());
        }
        return Optional.of(allowed);
    }
}