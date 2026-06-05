package ai.leadplus.application.campaigngenerator.tools;

import ai.leadplus.application.campaigngenerator.LeadSearchService;
import ai.leadplus.application.campaigns.CampaignService;
import ai.leadplus.application.leadcompany.LeadCompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeadGeneratorSearchTools {

    private final LeadSearchService leadSearchService;
    private final CampaignService campaignService;
    private final LeadCompanyService leadCompanyService;

    @Tool(
            name = "GetCampaignDetails",
            description = "Get details of a campaign by its ID"
    )
    public CleanedCampaign getCampaignDetails(
            @ToolParam(description = "Id of the current campaign") String campaignId
    ) {
        return CleanedCampaign.fromDto(campaignService.getCampaignById(Long.parseLong(campaignId)));
    }

    @Tool(
            name = "searchLeads",
            description = "Search for leads based on various criteria"
    )
    public LeadSearchToolResponse searchLeads(
            @ToolParam(description = "Id of the current campaign") String campaignId,
            @ToolParam(description = "Company names to search for (e.g., 'Siemens', 'Bosch', 'Latitude AI')", required = false) List<String> companyNames,
            @ToolParam(description = "State or province names in full format (e.g., 'California', 'Pennsylvania', 'Bavaria'). Never use abbreviations.", required = false) List<String> companyStates,
            @ToolParam(description = "City names in full format (e.g., 'Los Angeles', 'Munich', 'Pittsburgh')", required = false) List<String> companyCities,
            @ToolParam(
                    description = "Country names in full format — never use abbreviations (e.g., 'Germany' not 'DE', 'United States' not 'US', 'United Kingdom' not 'UK')",
                    required = false
            ) List<String> companyCountries,
            @ToolParam(
                    description = "Broad sales region names in CamelCase without spaces (e.g., 'Europe', 'EastCoast', 'DACH', 'APAC', 'WestCoast'). Use this when the user refers to a region, not a specific country or city.",
                    required = false
            ) List<String> companyRegions,
            @ToolParam(description = "Postal codes to filter by (e.g., '90001', '10001')", required = false) List<String> companyPostalCodes,
            @ToolParam(description = "SIC codes to filter by (e.g., '7372', '5812')", required = false) List<String> companySicCodes,
            @ToolParam(description = "NAICS codes to filter by (e.g., '541511', '523930')", required = false) List<String> companyNaicsCodes,
            @ToolParam(description = """
                    Technologies, tools, platforms, and domain terms mentioned by the user.

                    Extract ANY terms related to what companies use, build, or work with:
                    - Software & platforms (Python, AWS, Salesforce, Kubernetes)
                    - Hardware & equipment (PCB design, embedded systems, SMD assembly)
                    - Domain/specialty areas (autonomous vehicles, machine learning, battery management)

                    When in doubt about whether a term belongs here, include it rather than excluding it.
                    """,
                    required = false)
            List<String> techTerms,
            @ToolParam(description = "Company industries to filter by", required = false) List<String> companyIndustries,
            @ToolParam(
                    description = "Employee Count Range as text (e.g., '0-500', '501-1000', '1001-5000', '5001-10000', '10001+'). Use these exact ranges.",
                    required = false
            ) List<String> employeeCount,
            @ToolParam(
                    description = "Annual revenue ranges in USD using numeric values only (e.g., '0-1000000', '1000000-10000000', '10000000+')",
                    required = false
            ) List<String> revenueRanges,
            @ToolParam(
                    description = """
                            Specific job titles to filter by. Use this when the user names a role, a role+level combination, or any specific position.
                            Examples: 'CEO', 'Sales Director', 'IT Manager', 'Head of Engineering', 'Marketing Manager', 'VP of Sales'.
                            Rule: when the user says '<Function> <Level>' (e.g. 'Sales Directors', 'IT Managers'), always put the full phrase here as a single title — never split the function into departments and the level into seniority.""",
                    required = false
            ) List<String> titles,
            @ToolParam(
                    description = """
                            Standalone seniority level with no specific function — use ONLY when the user refers to a level without naming a role or department.
                            Examples: 'C-Suite', 'VP', 'Director', 'Manager', 'Senior', 'Junior'.
                            Do NOT use this when a function is also mentioned (e.g. 'Sales Directors' → titles, not seniority).""",
                    required = false
            ) List<String> seniority,
            @ToolParam(
                    description = """
                            Broad department/function area — use ONLY when the user says they want everyone in a department with no specific title or level mentioned (e.g., 'the entire Sales team', 'everyone in Engineering').
                            Examples: 'Engineering', 'Marketing', 'Operations', 'Finance', 'Research and Development'.
                            IMPORTANT: Do NOT use this when a level is mentioned alongside the function. 'Sales Directors' → titles: ['Sales Director'], NOT departments: ['Sales'].""",
                    required = false
            ) List<String> departments
    ) {
        return leadSearchService.searchLeads(
                campaignId,
                companyNames,
                companyCities,
                companyStates,
                companyCountries,
                companyRegions,
                techTerms,
                validateIndustries(companyIndustries),
                employeeCount,
                revenueRanges,
                null,
                null,
                titles,
                seniority,
                departments,
                companyPostalCodes,
                companySicCodes,
                companyNaicsCodes
        );
    }

    private List<String> validateIndustries(List<String> requestedIndustries) {
        if (CollectionUtils.isEmpty(requestedIndustries)) {
            return null;
        }

        Set<String> availableIndustries = leadCompanyService.getUniqueIndustries().stream()
                .filter(StringUtils::hasText)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());

        List<String> validIndustries = requestedIndustries.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .filter(value -> availableIndustries.contains(value.toLowerCase(Locale.ROOT)))
                .distinct()
                .toList();

        if (validIndustries.size() != requestedIndustries.size()) {
            List<String> droppedIndustries = new ArrayList<>(requestedIndustries);
            droppedIndustries.removeAll(validIndustries);
            if (!droppedIndustries.isEmpty()) {
                log.info("[TOOL] searchLeads dropped non-matching companyIndustries values: {}", droppedIndustries);
            }
        }

        return validIndustries.isEmpty() ? null : validIndustries;
    }
}
