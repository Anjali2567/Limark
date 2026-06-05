package ai.leadplus.application.campaignagent.tools;

import ai.leadplus.application.datapacks.DataPackGate;
import ai.leadplus.application.leadcompany.LeadCompanySearchService;
import ai.leadplus.application.leaddatapacks.GatedInfo;
import ai.leadplus.application.leads.LeadFilterCriteria;
import ai.leadplus.application.vendordatapacks.VendorAccess;
import ai.leadplus.domain.leadcompanies.LeadCompany;
import ai.leadplus.domain.leadcompanies.LeadCompanyRepository;
import ai.leadplus.domain.leadcontacts.LeadContact;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentLeadCountAggregationService {

    private static final int SAMPLE_COMPANY_COUNT = 5;
    private static final int COMPANY_ID_BATCH_SIZE = 20_000;

    private final LeadCompanyRepository leadCompanyRepository;
    private final LeadCompanySearchService leadCompanySearchService;
    private final DataPackGate dataPackGate;

    @PersistenceContext
    private EntityManager entityManager;

    private static final Map<String, String> US_STATE_ABBREVIATIONS = Map.ofEntries(
            Map.entry("alabama", "AL"), Map.entry("alaska", "AK"), Map.entry("arizona", "AZ"),
            Map.entry("arkansas", "AR"), Map.entry("california", "CA"), Map.entry("colorado", "CO"),
            Map.entry("connecticut", "CT"), Map.entry("delaware", "DE"), Map.entry("florida", "FL"),
            Map.entry("georgia", "GA"), Map.entry("hawaii", "HI"), Map.entry("idaho", "ID"),
            Map.entry("illinois", "IL"), Map.entry("indiana", "IN"), Map.entry("iowa", "IA"),
            Map.entry("kansas", "KS"), Map.entry("kentucky", "KY"), Map.entry("louisiana", "LA"),
            Map.entry("maine", "ME"), Map.entry("maryland", "MD"), Map.entry("massachusetts", "MA"),
            Map.entry("michigan", "MI"), Map.entry("minnesota", "MN"), Map.entry("mississippi", "MS"),
            Map.entry("missouri", "MO"), Map.entry("montana", "MT"), Map.entry("nebraska", "NE"),
            Map.entry("nevada", "NV"), Map.entry("new hampshire", "NH"), Map.entry("new jersey", "NJ"),
            Map.entry("new mexico", "NM"), Map.entry("new york", "NY"), Map.entry("north carolina", "NC"),
            Map.entry("north dakota", "ND"), Map.entry("ohio", "OH"), Map.entry("oklahoma", "OK"),
            Map.entry("oregon", "OR"), Map.entry("pennsylvania", "PA"), Map.entry("rhode island", "RI"),
            Map.entry("south carolina", "SC"), Map.entry("south dakota", "SD"), Map.entry("tennessee", "TN"),
            Map.entry("texas", "TX"), Map.entry("utah", "UT"), Map.entry("vermont", "VT"),
            Map.entry("virginia", "VA"), Map.entry("washington", "WA"), Map.entry("west virginia", "WV"),
            Map.entry("wisconsin", "WI"), Map.entry("wyoming", "WY"), Map.entry("district of columbia", "DC")
    );

    public AgentLeadCountResult count(
            LeadFilterCriteria filter,
            Long tenantId,
            GatedInfo gatedInfo,
            VendorAccess vendorAccess,
            Set<Long> ineligibleContactIds
    ) {
        Specification<LeadCompany> withContactSpec = buildWithContactSpec(filter, tenantId, gatedInfo, vendorAccess, ineligibleContactIds);

        long totalCompanies = leadCompanyRepository.count(withContactSpec);
        if (totalCompanies == 0) {
            return AgentLeadCountResult.builder()
                    .totalCompanies(0).totalContacts(0).sampleCompanyNames(List.of()).build();
        }

        List<LeadCompany> sample = leadCompanyRepository
                .findAll(withContactSpec, PageRequest.of(0, SAMPLE_COMPANY_COUNT)).getContent();
        List<String> sampleNames = sample.stream()
                .map(LeadCompany::getName).filter(Objects::nonNull).toList();

        long totalContacts = countMatchingContactsViaSubquery(filter, tenantId, gatedInfo, vendorAccess, ineligibleContactIds, withContactSpec);

        log.info("Agent lead count | tenantId={} | companies={} | contacts={} | sample={}",
                tenantId, totalCompanies, totalContacts, sampleNames);

        return AgentLeadCountResult.builder()
                .totalCompanies(totalCompanies)
                .totalContacts(totalContacts)
                .sampleCompanyNames(sampleNames)
                .build();
    }

    public List<Long> resolveCompanyIds(
            Long tenantId,
            LeadFilterCriteria filter,
            GatedInfo gatedInfo,
            VendorAccess vendorAccess,
            Set<Long> ineligibleContactIds
    ) {
        Specification<LeadCompany> withContactSpec =
                buildWithContactSpec(filter, tenantId, gatedInfo, vendorAccess, ineligibleContactIds);
        return paginateCompanyIds(withContactSpec);
    }

    private Specification<LeadCompany> buildWithContactSpec(LeadFilterCriteria filter,
                                                            Long tenantId,
                                                            GatedInfo gatedInfo,
                                                            VendorAccess vendorAccess,
                                                            Set<Long> ineligibleContactIds) {
        Specification<LeadCompany> companySpec = buildCompanySpecification(filter);

        if (gatedInfo != null && vendorAccess != null) {
            Specification<LeadCompany> gateSpec =
                    leadCompanySearchService.buildDataPackGateSpecification(gatedInfo, vendorAccess);
            if (gateSpec != null) {
                companySpec = companySpec.and(gateSpec);
            }
        }

        final Specification<LeadCompany> finalSpec = companySpec;
        return finalSpec.and((root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<LeadContact> cr = sub.from(LeadContact.class);
            sub.select(cr.get("leadCompanyId"));
            List<Predicate> cp = buildContactPredicates(cb, cr, filter, tenantId, gatedInfo, vendorAccess, ineligibleContactIds);
            sub.where(cb.and(cp.toArray(new Predicate[0])));
            return root.get("id").in(sub);
        });
    }

    private Specification<LeadCompany> buildCompanySpecification(LeadFilterCriteria filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("active"), true));

            if (!CollectionUtils.isEmpty(filter.getCompanyNames())) {
                List<Predicate> namePredicates = filter.getCompanyNames().stream()
                        .filter(StringUtils::hasText)
                        .map(v -> {
                            String pattern = "%" + v.trim().toLowerCase() + "%";
                            return cb.or(
                                    cb.like(cb.lower(root.get("name")), pattern),
                                    cb.like(cb.lower(root.get("domain")), pattern)
                            );
                        })
                        .toList();
                if (!namePredicates.isEmpty()) {
                    predicates.add(cb.or(namePredicates.toArray(new Predicate[0])));
                }
            }

            if (!CollectionUtils.isEmpty(filter.getIndustries())) {
                List<Predicate> industryPredicates = filter.getIndustries().stream()
                        .filter(StringUtils::hasText)
                        .map(ind -> {
                            String pattern = "%" + ind.toLowerCase() + "%";
                            return cb.or(
                                    cb.like(cb.lower(root.get("industry")), pattern),
                                    cb.like(cb.lower(root.get("segment")), pattern)
                            );
                        })
                        .toList();
                if (!industryPredicates.isEmpty()) {
                    predicates.add(cb.or(industryPredicates.toArray(new Predicate[0])));
                }
            }

            if (!CollectionUtils.isEmpty(filter.getEmployeeRanges())) {
                predicates.add(root.get("employeeRange").in(filter.getEmployeeRanges()));
            }

            if (!CollectionUtils.isEmpty(filter.getRevenueRanges())) {
                List<Predicate> revenuePredicates = filter.getRevenueRanges().stream()
                        .filter(StringUtils::hasText)
                        .map(range -> buildRevenuePredicate(cb, root, range))
                        .filter(Objects::nonNull)
                        .toList();
                if (!revenuePredicates.isEmpty()) {
                    predicates.add(cb.or(revenuePredicates.toArray(new Predicate[0])));
                }
            }

            if (!CollectionUtils.isEmpty(filter.getKeywords())
                    || !CollectionUtils.isEmpty(filter.getTechnologies())
                    || !CollectionUtils.isEmpty(filter.getToolsServices())) {

                List<String> allTerms = new ArrayList<>();
                if (!CollectionUtils.isEmpty(filter.getKeywords()))      allTerms.addAll(filter.getKeywords());
                if (!CollectionUtils.isEmpty(filter.getTechnologies()))  allTerms.addAll(filter.getTechnologies());
                if (!CollectionUtils.isEmpty(filter.getToolsServices())) allTerms.addAll(filter.getToolsServices());

                List<Predicate> techPredicates = allTerms.stream()
                        .filter(StringUtils::hasText)
                        .map(term -> {
                            String pattern = "%" + term.trim().toLowerCase() + "%";
                            return cb.or(
                                    cb.like(cb.lower(arrayToString(cb, root, "keywords")),           pattern),
                                    cb.like(cb.lower(arrayToString(cb, root, "technologies")),        pattern),
                                    cb.like(cb.lower(arrayToString(cb, root, "scrapedTechnologies")), pattern),
                                    cb.like(cb.lower(arrayToString(cb, root, "scrapedTools")),        pattern),
                                    cb.like(cb.lower(arrayToString(cb, root, "scrapedServices")),     pattern)
                            );
                        })
                        .toList();

                if (!techPredicates.isEmpty()) {
                    predicates.add(cb.or(techPredicates.toArray(new Predicate[0])));
                }
            }

            if (!CollectionUtils.isEmpty(filter.getRegions())) {
                List<Predicate> regionPredicates = filter.getRegions().stream()
                        .filter(StringUtils::hasText)
                        .map(r -> cb.like(cb.lower(root.get("region")), "%" + r.toLowerCase() + "%"))
                        .toList();
                if (!regionPredicates.isEmpty()) {
                    predicates.add(cb.or(regionPredicates.toArray(new Predicate[0])));
                }
            }

            List<String> companyCities    = nullSafe(filter.getCompanyCities());
            List<String> companyStates    = nullSafe(filter.getCompanyStates());
            List<String> companyCountries = nullSafe(filter.getCompanyCountries());

            if (!companyCities.isEmpty() || !companyStates.isEmpty() || !companyCountries.isEmpty()) {
                List<Predicate> locationPredicates = new ArrayList<>();

                List<String> allLocationValues = new ArrayList<>();
                allLocationValues.addAll(companyCities);
                allLocationValues.addAll(companyStates);
                allLocationValues.addAll(companyCountries);

                locationPredicates.add(cb.or(
                        root.get("hqCity").in(allLocationValues),
                        root.get("hqState").in(allLocationValues),
                        root.get("hqCountry").in(allLocationValues)
                ));

                for (String state : companyStates) {
                    String abbrev = US_STATE_ABBREVIATIONS.get(state.trim().toLowerCase());
                    if (abbrev != null) {
                        locationPredicates.add(cb.equal(cb.upper(root.get("hqState")), abbrev));
                    }
                }

                predicates.add(cb.or(locationPredicates.toArray(new Predicate[0])));
            }

            if (!CollectionUtils.isEmpty(filter.getPostalCodes())) {
                List<Predicate> postalPredicates = filter.getPostalCodes().stream()
                        .filter(StringUtils::hasText)
                        .map(p -> cb.like(cb.lower(root.get("postalCode")), "%" + p.toLowerCase() + "%"))
                        .toList();
                if (!postalPredicates.isEmpty()) {
                    predicates.add(cb.or(postalPredicates.toArray(new Predicate[0])));
                }
            }

            if (!CollectionUtils.isEmpty(filter.getSicCodes())) {
                List<Predicate> sicPredicates = filter.getSicCodes().stream()
                        .filter(StringUtils::hasText)
                        .map(code -> cb.like(
                                cb.lower(arrayToString(cb, root, "sicCodes")),
                                "%" + code.toLowerCase() + "%"))
                        .toList();
                if (!sicPredicates.isEmpty()) {
                    predicates.add(cb.or(sicPredicates.toArray(new Predicate[0])));
                }
            }

            if (!CollectionUtils.isEmpty(filter.getNaicsCodes())) {
                List<Predicate> naicsPredicates = filter.getNaicsCodes().stream()
                        .filter(StringUtils::hasText)
                        .map(code -> cb.like(
                                cb.lower(arrayToString(cb, root, "naicsCodes")),
                                "%" + code.toLowerCase() + "%"))
                        .toList();
                if (!naicsPredicates.isEmpty()) {
                    predicates.add(cb.or(naicsPredicates.toArray(new Predicate[0])));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildContactPredicates(CriteriaBuilder cb,
                                                   Root<LeadContact> root,
                                                   LeadFilterCriteria filter,
                                                   Long tenantId,
                                                   GatedInfo gatedInfo,
                                                   VendorAccess vendorAccess,
                                                   Set<Long> ineligibleContactIds) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("active"), true));
        predicates.add(cb.and(
                cb.isNotNull(root.get("email")),
                cb.notEqual(root.get("email"), "")
        ));

        if (tenantId != null && (vendorAccess == null || vendorAccess.getTenantId() == null
                || !tenantId.equals(vendorAccess.getTenantId()))) {
            String tenantIdStr = tenantId.toString();
            predicates.add(cb.or(
                    cb.isNull(root.get("tenantIds")),
                    cb.isTrue(cb.function("array_contains", Boolean.class, root.get("tenantIds"), cb.literal(tenantIdStr)))
            ));
        }

        predicates.addAll(dataPackGate.buildGatePredicates(root, cb, gatedInfo, vendorAccess));

        if (!CollectionUtils.isEmpty(ineligibleContactIds)) {
            predicates.add(cb.not(root.get("id").in(ineligibleContactIds)));
        }

        if (!CollectionUtils.isEmpty(filter.getTitles())) {
            List<Predicate> titlePredicates = filter.getTitles().stream()
                    .filter(StringUtils::hasText)
                    .map(t -> {
                        String pattern = "%" + t.trim().toLowerCase() + "%";
                        return cb.or(
                                cb.like(cb.lower(root.get("title")), pattern),
                                cb.like(cb.lower(arrayToString(cb, root, "normalizedTitleTokens")), pattern)
                        );
                    })
                    .toList();
            if (!titlePredicates.isEmpty()) {
                predicates.add(cb.or(titlePredicates.toArray(new Predicate[0])));
            }
        }

        if (!CollectionUtils.isEmpty(filter.getSeniority())) {
            List<Predicate> seniorityPredicates = filter.getSeniority().stream()
                    .filter(StringUtils::hasText)
                    .map(s -> cb.like(cb.lower(root.get("seniority")), "%" + s.toLowerCase() + "%"))
                    .toList();
            if (!seniorityPredicates.isEmpty()) {
                predicates.add(cb.or(seniorityPredicates.toArray(new Predicate[0])));
            }
        }

        if (!CollectionUtils.isEmpty(filter.getDepartments())) {
            List<Predicate> deptPredicates = filter.getDepartments().stream()
                    .filter(StringUtils::hasText)
                    .map(d -> cb.like(cb.lower(root.get("department")), "%" + d.toLowerCase() + "%"))
                    .toList();
            if (!deptPredicates.isEmpty()) {
                predicates.add(cb.or(deptPredicates.toArray(new Predicate[0])));
            }
        }

        List<String> cities    = nullSafe(filter.getCities());
        List<String> states    = nullSafe(filter.getStates());
        List<String> countries = nullSafe(filter.getCountries());

        if (!cities.isEmpty() || !states.isEmpty() || !countries.isEmpty()) {
            List<Predicate> locationPredicates = new ArrayList<>();

            List<String> allValues = new ArrayList<>();
            allValues.addAll(cities);
            allValues.addAll(states);
            allValues.addAll(countries);

            locationPredicates.add(cb.or(
                    root.get("locationCity").in(allValues),
                    root.get("locationState").in(allValues),
                    root.get("locationCountry").in(allValues)
            ));

            for (String state : states) {
                String abbrev = US_STATE_ABBREVIATIONS.get(state.trim().toLowerCase());
                if (abbrev != null) {
                    locationPredicates.add(cb.equal(cb.upper(root.get("locationState")), abbrev));
                }
            }

            predicates.add(cb.or(locationPredicates.toArray(new Predicate[0])));
        }

        return predicates;
    }

    private long countMatchingContactsViaSubquery(LeadFilterCriteria filter,
                                                  Long tenantId,
                                                  GatedInfo gatedInfo,
                                                  VendorAccess vendorAccess,
                                                  Set<Long> ineligibleContactIds,
                                                  Specification<LeadCompany> companySpec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<LeadContact> contactRoot = query.from(LeadContact.class);

        List<Predicate> predicates = buildContactPredicates(cb, contactRoot, filter, tenantId, gatedInfo, vendorAccess, ineligibleContactIds);

        Subquery<Long> companySub = query.subquery(Long.class);
        Root<LeadCompany> companyRoot = companySub.from(LeadCompany.class);
        Predicate companyPredicate = companySpec.toPredicate(companyRoot, query, cb);
        companySub.select(companyRoot.get("id"));
        if (companyPredicate != null) {
            companySub.where(companyPredicate);
        }

        predicates.add(contactRoot.get("leadCompanyId").in(companySub));

        query.select(cb.count(contactRoot));
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        Long result = entityManager.createQuery(query).getSingleResult();
        return result != null ? result : 0L;
    }

    private List<Long> paginateCompanyIds(Specification<LeadCompany> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<LeadCompany> root = query.from(LeadCompany.class);
        Predicate predicate = spec.toPredicate(root, query, cb);
        if (predicate != null) query.where(predicate);
        query.select(root.get("id"));

        List<Long> companyIds = new ArrayList<>();
        int offset = 0;

        while (true) {
            List<Long> batch = entityManager.createQuery(query)
                    .setFirstResult(offset)
                    .setMaxResults(COMPANY_ID_BATCH_SIZE)
                    .getResultList();

            if (CollectionUtils.isEmpty(batch)) break;
            companyIds.addAll(batch);
            if (batch.size() < COMPANY_ID_BATCH_SIZE) break;
            offset += COMPANY_ID_BATCH_SIZE;
        }

        return companyIds;
    }

    private <T> Expression<String> arrayToString(CriteriaBuilder cb, Root<T> root, String attribute) {
        return cb.function("array_to_string", String.class,
                root.get(attribute), cb.literal(","));
    }

    private Predicate buildRevenuePredicate(CriteriaBuilder cb, Root<LeadCompany> root, String range) {
        if (!StringUtils.hasText(range)) return null;
        try {
            if (range.endsWith("+")) {
                long min = Long.parseLong(range.replace("+", "").trim());
                return cb.greaterThanOrEqualTo(root.get("revenueUsdAmount"), min);
            }
            String[] parts = range.split("-");
            if (parts.length == 2) {
                long min = Long.parseLong(parts[0].trim());
                long max = Long.parseLong(parts[1].trim());
                return cb.between(root.get("revenueUsdAmount"), min, max);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid revenue range format: {}", range);
        }
        return null;
    }

    private List<String> nullSafe(List<String> list) {
        return list != null ? list : List.of();
    }
}