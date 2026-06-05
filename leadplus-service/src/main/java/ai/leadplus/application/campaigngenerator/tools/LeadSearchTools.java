package ai.leadplus.application.campaigngenerator.tools;

import ai.leadplus.application.campaigns.CampaignService;
import ai.leadplus.application.campaigngenerator.LeadSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeadSearchTools {

    private final LeadSearchService leadSearchService;
    private final CampaignService campaignService;

    @Tool(
            name = "GetCampaignDetails",
            description = "Get details of a campaign by its ID"
    )
    public CleanedCampaign getCampaignDetails(
            @ToolParam(description = "Id of the current campaign") Long campaignId
    ) {
        return CleanedCampaign.fromDto(campaignService.getCampaignById(campaignId));
    }

    @Tool(
            name = "LeadCompanyGenericSearch",
            description = "Search For Companies without a specific query"
    )
    public LeadSearchToolResponse leadCompanySearch(
            @ToolParam(description = "Id of the current campaign") String campaignId,
            @ToolParam(description = "Company name keywords to search for", required = false) List<String> nameKeywords,
            @ToolParam(
                    description = "Employee Count Range as text (e.g., '0-500', '501-1000', '1001-5000', '5001-10000', '10000+'). Use these exact ranges ",
                    required = false
            ) List<String> employeeCount,
            @ToolParam(description = "State Names in Full Format (e.g., 'California', 'New York')", required = false) List<String> states,
            @ToolParam(description = "City Names in Full Format (e.g., 'Los Angeles', 'London')", required = false) List<String> cities,
            @ToolParam(description = "Country names in full format (e.g., 'United States' instead of 'US', 'United Kingdom' instead of 'UK')", required = false) List<String> countries,
            @ToolParam(description = "Region names without spaces (e.g., 'EastCoast' instead of 'East Coast')", required = false) List<String> regions
    ) {
        return leadSearchService.performGenericSearch(
                campaignId, nameKeywords, employeeCount, states, cities, countries, regions, false
        );
    }

    @Tool(
            name = "LeadCompanyExcludeLocationGenericSearch",
            description = "Search for companies excluding specific locations without a specific query"
    )
    public LeadSearchToolResponse leadCompanyExcludeFilterSearch(
            @ToolParam(description = "Id of the current campaign") String campaignId,
            @ToolParam(description = "Company name keywords to search for", required = false) List<String> nameKeywords,
            @ToolParam(description = "Employee Count Range as text (e.g., '0-500', '501-1000', '1001-5000', '5001-10000', '10000+'). Use these exact ranges", required = false) List<String> employeeCount,
            @ToolParam(description = "State Names to EXCLUDE (e.g., 'California', 'New York')", required = false) List<String> excludeStates,
            @ToolParam(description = "City Names to EXCLUDE (e.g., 'Los Angeles', 'London')", required = false) List<String> excludeCities,
            @ToolParam(description = "Country names to EXCLUDE (e.g., 'United States', 'United Kingdom')", required = false) List<String> excludeCountries,
            @ToolParam(description = "Region names to EXCLUDE (e.g., 'EastCoast', 'WestCoast')", required = false) List<String> excludeRegions
    ) {
        return leadSearchService.performGenericSearch(
                campaignId, nameKeywords, employeeCount, excludeStates, excludeCities, excludeCountries, excludeRegions, true
        );
    }

    @Tool(
            name = "NarrowDownContactsByTitlesAndLocation",
            description = "Narrow down contacts by job titles and location filters, Location is independent of company location filters"
    )
    public LeadSearchToolResponse narrowDownContactsByTitlesAndLocation(
            @ToolParam(description = "Id of the current campaign") String campaignId,
            @ToolParam(description = "Job titles to filter by (e.g., 'CEO', 'Marketing Manager', 'Software Engineer')") List<String> jobTitles,
            @ToolParam(description = "State Names in Full Format (e.g., 'California', 'New York')", required = false) List<String> states,
            @ToolParam(description = "City Names in Full Format (e.g., 'Los Angeles', 'London')", required = false) List<String> cities,
            @ToolParam(description = "Country names in full format (e.g., 'United States' instead of 'US', 'United Kingdom' instead of 'UK')", required = false) List<String> countries
    ) {

        return leadSearchService.narrowDownContacts(campaignId, jobTitles, states, cities, countries);
    }
}
