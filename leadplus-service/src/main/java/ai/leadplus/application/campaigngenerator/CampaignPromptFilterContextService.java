package ai.leadplus.application.campaigngenerator;

import ai.leadplus.domain.leadcompanies.LeadCompanyRepository;
import ai.leadplus.domain.leadcontacts.LeadContactRepository;
import ai.leadplus.domain.leads.LeadFacetValue;
import ai.leadplus.domain.tenants.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignPromptFilterContextService {

    private static final int PER_FACET_LIMIT = 100;
    private static final int MAX_TOTAL_VALUES = 500;

    private final LeadCompanyRepository leadCompanyRepository;
    private final LeadContactRepository leadContactRepository;
    private final TenantRepository tenantRepository;

    public String buildContextBlock(Long tenantId) {
        // TODO: Filter by tenant-accessible contacts and companies instead of hardcoded segment matching.
        var tenantOpt = tenantRepository.findById(tenantId);
        String tenantName = tenantOpt
                .map(tenant -> tenant.getName())
                .filter(StringUtils::hasText)
                .map(String::trim)
                .orElse("current tenant");
        String tenantProfileContext = tenantOpt
                .map(tenant -> tenant.getProfileContext())
                .filter(StringUtils::hasText)
                .map(String::trim)
                .orElse("none provided");

        int remaining = MAX_TOTAL_VALUES;
        List<LeadFacetValue> industries = fetchWithLimit(remaining, limit ->
                leadCompanyRepository.findTopIndustryValuesByTenantId(tenantId, limit));
        remaining -= industries.size();

        List<LeadFacetValue> naicsCodes = fetchWithLimit(remaining, limit ->
                leadCompanyRepository.findTopNaicsValuesByTenantId(tenantId, limit));
        remaining -= naicsCodes.size();

        List<LeadFacetValue> sicCodes = fetchWithLimit(remaining, limit ->
                leadCompanyRepository.findTopSicValuesByTenantId(tenantId, limit));
        remaining -= sicCodes.size();

        List<LeadFacetValue> titles = fetchWithLimit(remaining, limit ->
                leadContactRepository.findTopTitleValuesByTenantId(tenantId, limit));
        remaining -= titles.size();

        List<LeadFacetValue> departments = fetchWithLimit(remaining, limit ->
                leadContactRepository.findTopDepartmentValuesByTenantId(tenantId, limit));

        return """
                ### Tenant Identity
                - Tenant Name: %s
                - Self-reference aliases: us, our, we, for us, %s

                ### Tenant Business Context
                %s

                ## Tenant Filter Context
                Use only these tenant-scoped values when creating filters. Prefer exact matches.
                - Industries: %s
                - NAICS Codes: %s
                - SIC Codes: %s
                - Contact Titles: %s
                - Contact Departments: %s
                """.formatted(
                tenantName,
                tenantName,
                tenantProfileContext,
                formatValues(industries),
                formatValues(naicsCodes),
                formatValues(sicCodes),
                formatValues(titles),
                formatValues(departments)
        );
    }

    private List<LeadFacetValue> fetchWithLimit(int remaining, ValueSupplier supplier) {
        if (remaining <= 0) {
            return Collections.emptyList();
        }
        int limit = Math.min(PER_FACET_LIMIT, remaining);
        List<LeadFacetValue> values = supplier.get(limit);
        if (CollectionUtils.isEmpty(values)) {
            return Collections.emptyList();
        }
        return values;
    }

    private String formatValues(List<LeadFacetValue> values) {
        if (CollectionUtils.isEmpty(values)) {
            return "none";
        }
        return values.stream()
                .map(v -> "%s (%d)".formatted(v.getValue(), v.getCount().longValue()))
                .collect(java.util.stream.Collectors.joining(", "));
    }

    @FunctionalInterface
    private interface ValueSupplier {
        List<LeadFacetValue> get(int limit);
    }
}
