package ai.leadplus.application.leads;

import ai.leadplus.application.datapacks.DataPackGate;
import ai.leadplus.application.leadcompany.LeadCompanySearchService;
import ai.leadplus.application.leadcompany.LeadCompanyWithContactCountDto;
import ai.leadplus.application.leaddatapacks.GatedInfo;
import ai.leadplus.application.vendordatapacks.VendorAccess;
import ai.leadplus.domain.leadcompanies.LeadCompany;
import ai.leadplus.domain.leadcompanies.LeadCompanyRepository;
import ai.leadplus.domain.leadcontacts.LeadContact;
import ai.leadplus.domain.leadcontacts.LeadContactRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyLeadSearchService {

    private static final int MAX_ID_RESULTS = 25_000;

    private final LeadCompanyRepository leadCompanyRepository;
    private final LeadContactRepository leadContactRepository;
    private final LeadCompanySearchService leadCompanySearchService;
    private final DataPackGate dataPackGate;
    private final EntityManager entityManager;

    public Page<LeadCompanyWithContactCountDto> searchLeadCompanies(
            LeadFilterCriteria req,
            String query,
            GatedInfo gatedInfo,
            VendorAccess vendorAccess,
            Pageable pageable) {

        Specification<LeadCompany> companySpec = buildCompanySpecification(req, query);

        if (gatedInfo != null && vendorAccess != null) {
            Specification<LeadCompany> gateSpec = leadCompanySearchService.buildDataPackGateSpecification(gatedInfo, vendorAccess);
            if (gateSpec != null) {
                companySpec = companySpec.and(gateSpec);
            }
        }

        return runLookupAggregation(companySpec, gatedInfo, vendorAccess, pageable);
    }

    private Page<LeadCompanyWithContactCountDto> runLookupAggregation(
            Specification<LeadCompany> companySpec,
            GatedInfo gatedInfo,
            VendorAccess vendorAccess,
            Pageable pageable) {

        // Use subquery to ensure company has at least one active, gated-visible contact with an email
        Specification<LeadCompany> combinedSpec = companySpec.and((root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<LeadContact> contactRoot = subquery.from(LeadContact.class);
            subquery.select(contactRoot.get("leadCompanyId"));

            List<Predicate> contactPredicates = new ArrayList<>();
            contactPredicates.add(cb.equal(contactRoot.get("active"), true));
            contactPredicates.add(cb.isNotNull(contactRoot.get("email")));
            contactPredicates.add(cb.notEqual(contactRoot.get("email"), ""));

            // Apply contact-level data pack gate inside the existence subquery
            contactPredicates.addAll(dataPackGate.buildGatePredicates(contactRoot, cb, gatedInfo, vendorAccess));

            subquery.where(cb.and(contactPredicates.toArray(new Predicate[0])));

            return root.get("id").in(subquery);
        });

        Page<LeadCompany> companiesPage = leadCompanyRepository.findAll(combinedSpec, pageable);
        List<LeadCompany> companies = companiesPage.getContent();

        if (companies.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, companiesPage.getTotalElements());
        }

        List<Long> pageCompanyIds = companies.stream()
                .map(LeadCompany::getId)
                .toList();

        Map<Long, Long> contactCountMap = getContactCountsByCompanyIds(pageCompanyIds, gatedInfo, vendorAccess);

        List<LeadCompanyWithContactCountDto> content = companies.stream()
                .map(company -> LeadCompanyWithContactCountDto.fromEntity(
                        company,
                        contactCountMap.getOrDefault(company.getId(), 0L)
                ))
                .toList();

        return new PageImpl<>(content, pageable, companiesPage.getTotalElements());
    }

    private Map<Long, Long> getContactCountsByCompanyIds(List<Long> companyIds,
                                                          GatedInfo gatedInfo,
                                                          VendorAccess vendorAccess) {
        if (CollectionUtils.isEmpty(companyIds)) {
            return Collections.emptyMap();
        }

        // When no gating is active, use the efficient JPQL aggregate query
        if (gatedInfo == null || (!gatedInfo.isNullGated()
                && CollectionUtils.isEmpty(gatedInfo.getNamedGatedSegments()))) {
            List<ai.leadplus.domain.leadcontacts.CompanyContactCount> counts =
                    leadContactRepository.countContactsByCompanyIds(companyIds);
            return counts.stream()
                    .collect(Collectors.toMap(
                            ai.leadplus.domain.leadcontacts.CompanyContactCount::getCompanyId,
                            ai.leadplus.domain.leadcontacts.CompanyContactCount::getCount
                    ));
        }

        // Gating is active — use specification-based query and count in memory
        Specification<LeadContact> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("active"), true));
            predicates.add(cb.isNotNull(root.get("email")));
            predicates.add(cb.notEqual(root.get("email"), ""));
            predicates.add(root.get("leadCompanyId").in(companyIds));

            // Apply contact-level data pack gate
            predicates.addAll(dataPackGate.buildGatePredicates(root, cb, gatedInfo, vendorAccess));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<LeadContact> contacts = leadContactRepository.findAll(spec);

        return contacts.stream()
                .collect(Collectors.groupingBy(
                        LeadContact::getLeadCompanyId,
                        Collectors.counting()
                ));
    }

    private Specification<LeadCompany> buildCompanySpecification(LeadFilterCriteria req, String queryText) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("active"), true));

            if (StringUtils.hasText(queryText)) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + queryText.trim().toLowerCase() + "%"));
            }

            if (!CollectionUtils.isEmpty(req.getCompanyNames())) {
                List<Predicate> companyPredicates = req.getCompanyNames().stream()
                        .filter(StringUtils::hasText)
                        .map(v -> v.trim().toLowerCase())
                        .map(value -> {
                            String pattern = "%" + value + "%";
                            return cb.or(
                                    cb.like(cb.lower(cb.trim(root.get("name"))), pattern),
                                    cb.like(cb.lower(cb.trim(root.get("domain"))), pattern)
                            );
                        })
                        .toList();
                if (!companyPredicates.isEmpty()) {
                    predicates.add(cb.or(companyPredicates.toArray(new Predicate[0])));
                }
            }

            if (!CollectionUtils.isEmpty(req.getRegions())) {
                predicates.add(cb.or(req.getRegions().stream()
                        .map(reg -> cb.equal(cb.lower(root.get("region")), reg.toLowerCase()))
                        .toArray(Predicate[]::new)));
            }

            List<String> locationValues = new ArrayList<>();
            if (!CollectionUtils.isEmpty(req.getCompanyCities()))
                locationValues.addAll(req.getCompanyCities());
            if (!CollectionUtils.isEmpty(req.getCompanyStates()))
                locationValues.addAll(req.getCompanyStates());
            if (!CollectionUtils.isEmpty(req.getCompanyCountries()))
                locationValues.addAll(req.getCompanyCountries());

            if (!locationValues.isEmpty()) {
                List<String> lowerLocations = locationValues.stream().map(String::toLowerCase).toList();
                predicates.add(cb.or(
                        cb.lower(root.get("hqCity")).in(lowerLocations),
                        cb.lower(root.get("hqState")).in(lowerLocations),
                        cb.lower(root.get("hqCountry")).in(lowerLocations)
                ));
            }

            if (!CollectionUtils.isEmpty(req.getIndustries())) {
                predicates.add(cb.or(req.getIndustries().stream()
                        .map(ind -> cb.or(
                                cb.like(cb.lower(root.get("industry")), "%" + ind.toLowerCase() + "%")
                        ))
                        .toArray(Predicate[]::new)));
            }

            if (!CollectionUtils.isEmpty(req.getEmployeeRanges())) {
                predicates.add(root.get("employeeRange").in(req.getEmployeeRanges()));
            }

            if (!CollectionUtils.isEmpty(req.getPostalCodes())) {
                predicates.add(root.get("postalCode").in(req.getPostalCodes()));
            }

            if (!CollectionUtils.isEmpty(req.getRevenueRanges())) {
                List<Predicate> revenuePreds = new ArrayList<>();
                for (String range : req.getRevenueRanges()) {
                    if (range.endsWith("+")) {
                        try {
                            long min = Long.parseLong(range.substring(0, range.length() - 1).trim());
                            revenuePreds.add(cb.greaterThanOrEqualTo(root.get("revenueUsdAmount"), min));
                        } catch (NumberFormatException ignored) {}
                    } else if (range.contains("-")) {
                        try {
                            String[] parts = range.split("-");
                            long min = Long.parseLong(parts[0].trim());
                            long max = Long.parseLong(parts[1].trim());
                            revenuePreds.add(cb.between(root.get("revenueUsdAmount"), min, max));
                        } catch (NumberFormatException ignored) {}
                    }
                }
                if (!revenuePreds.isEmpty()) {
                    predicates.add(cb.or(revenuePreds.toArray(new Predicate[0])));
                }
            }

            if (req.isAggregateTechSearch()) {
                List<String> allTerms = new ArrayList<>();
                if (!CollectionUtils.isEmpty(req.getKeywords())) allTerms.addAll(req.getKeywords());
                if (!CollectionUtils.isEmpty(req.getTechnologies())) allTerms.addAll(req.getTechnologies());
                if (!CollectionUtils.isEmpty(req.getToolsServices())) allTerms.addAll(req.getToolsServices());
                if (!allTerms.isEmpty()) {
                    List<Predicate> termPreds = new ArrayList<>();
                    for (String term : allTerms) {
                        String p = "%" + term.toLowerCase() + "%";
                        termPreds.add(cb.or(
                                cb.like(cb.lower(cb.function("array_to_string", String.class, root.get("keywords"), cb.literal(","))), p),
                                cb.like(cb.lower(cb.function("array_to_string", String.class, root.get("technologies"), cb.literal(","))), p),
                                cb.like(cb.lower(cb.function("array_to_string", String.class, root.get("scrapedTechnologies"), cb.literal(","))), p),
                                cb.like(cb.lower(cb.function("array_to_string", String.class, root.get("scrapedTools"), cb.literal(","))), p),
                                cb.like(cb.lower(cb.function("array_to_string", String.class, root.get("scrapedServices"), cb.literal(","))), p),
                                cb.like(cb.lower(root.get("industry")), p)
                        ));
                    }
                    predicates.add(cb.or(termPreds.toArray(new Predicate[0])));
                }
            } else {
                if (!CollectionUtils.isEmpty(req.getKeywords())) {
                    List<Predicate> kwPreds = new ArrayList<>();
                    for (String kw : req.getKeywords()) {
                        String p = "%" + kw.toLowerCase() + "%";
                        kwPreds.add(cb.like(cb.lower(cb.function("array_to_string", String.class, root.get("keywords"), cb.literal(","))), p));
                    }
                    predicates.add(cb.or(kwPreds.toArray(new Predicate[0])));
                }
                if (!CollectionUtils.isEmpty(req.getTechnologies())) {
                    List<Predicate> techPreds = new ArrayList<>();
                    for (String tech : req.getTechnologies()) {
                        String p = "%" + tech.toLowerCase() + "%";
                        techPreds.add(cb.or(
                                cb.like(cb.lower(cb.function("array_to_string", String.class, root.get("technologies"), cb.literal(","))), p),
                                cb.like(cb.lower(cb.function("array_to_string", String.class, root.get("scrapedTechnologies"), cb.literal(","))), p)
                        ));
                    }
                    predicates.add(cb.or(techPreds.toArray(new Predicate[0])));
                }
                if (!CollectionUtils.isEmpty(req.getToolsServices())) {
                    List<Predicate> toolsPreds = new ArrayList<>();
                    for (String tool : req.getToolsServices()) {
                        String p = "%" + tool.toLowerCase() + "%";
                        toolsPreds.add(cb.or(
                                cb.like(cb.lower(cb.function("array_to_string", String.class, root.get("scrapedTools"), cb.literal(","))), p),
                                cb.like(cb.lower(cb.function("array_to_string", String.class, root.get("scrapedServices"), cb.literal(","))), p)
                        ));
                    }
                    predicates.add(cb.or(toolsPreds.toArray(new Predicate[0])));
                }
            }

            if (!CollectionUtils.isEmpty(req.getSicCodes())) {
                List<Predicate> sicPreds = new ArrayList<>();
                for (String code : req.getSicCodes()) {
                    Expression<String> wrapped = cb.lower(cb.concat(cb.concat(cb.literal("|"),
                            cb.function("array_to_string", String.class, root.get("sicCodes"), cb.literal("|"))),
                            cb.literal("|")));
                    sicPreds.add(cb.like(wrapped, "%|" + code.toLowerCase() + "|%"));
                }
                predicates.add(cb.or(sicPreds.toArray(new Predicate[0])));
            }

            if (!CollectionUtils.isEmpty(req.getNaicsCodes())) {
                List<Predicate> naicsPreds = new ArrayList<>();
                for (String code : req.getNaicsCodes()) {
                    Expression<String> wrapped = cb.lower(cb.concat(cb.concat(cb.literal("|"),
                            cb.function("array_to_string", String.class, root.get("naicsCodes"), cb.literal("|"))),
                            cb.literal("|")));
                    naicsPreds.add(cb.like(wrapped, "%|" + code.toLowerCase() + "|%"));
                }
                predicates.add(cb.or(naicsPreds.toArray(new Predicate[0])));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public List<CompanyIdWithDomainDto> findCompanyIdsWithDomains(LeadFilterCriteria req, String query,
                                                                  GatedInfo gatedInfo, VendorAccess vendorAccess) {
        Specification<LeadCompany> combinedSpec = buildCompanySpecWithContactExistence(req, query, gatedInfo, vendorAccess);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<LeadCompany> root = cq.from(LeadCompany.class);
        cq.multiselect(root.get("id"), root.get("domain"));

        Predicate predicate = combinedSpec.toPredicate(root, cq, cb);
        if (predicate != null) {
            cq.where(predicate);
        }

        List<Tuple> results = entityManager.createQuery(cq)
                .setMaxResults(MAX_ID_RESULTS)
                .getResultList();

        return results.stream()
                .map(tuple -> CompanyIdWithDomainDto.builder()
                        .id(tuple.get(0, Long.class))
                        .domain(tuple.get(1, String.class))
                        .build())
                .toList();
    }

    private Specification<LeadCompany> buildCompanySpecWithContactExistence(
            LeadFilterCriteria req, String query,
            GatedInfo gatedInfo, VendorAccess vendorAccess) {
        Specification<LeadCompany> companySpec = buildCompanySpecification(req, query);

        if (gatedInfo != null && vendorAccess != null) {
            Specification<LeadCompany> gateSpec = leadCompanySearchService.buildDataPackGateSpecification(gatedInfo, vendorAccess);
            if (gateSpec != null) {
                companySpec = companySpec.and(gateSpec);
            }
        }

        // Add contact-existence subquery (same as runLookupAggregation)
        Specification<LeadCompany> finalSpec = companySpec;
        return finalSpec.and((root, cq, cb) -> {
            Subquery<Long> subquery = cq.subquery(Long.class);
            Root<LeadContact> contactRoot = subquery.from(LeadContact.class);
            subquery.select(contactRoot.get("leadCompanyId"));

            List<Predicate> contactPredicates = new ArrayList<>();
            contactPredicates.add(cb.equal(contactRoot.get("active"), true));
            contactPredicates.add(cb.isNotNull(contactRoot.get("email")));
            contactPredicates.add(cb.notEqual(contactRoot.get("email"), ""));
            contactPredicates.addAll(dataPackGate.buildGatePredicates(contactRoot, cb, gatedInfo, vendorAccess));

            subquery.where(cb.and(contactPredicates.toArray(new Predicate[0])));
            return root.get("id").in(subquery);
        });
    }

    public Page<LeadCompanyWithContactCountDto> searchLeadCompaniesByIds(
            List<Long> companyIds,
            GatedInfo gatedInfo,
            VendorAccess vendorAccess,
            Pageable pageable) {

        if (CollectionUtils.isEmpty(companyIds)) {
            return Page.empty(pageable);
        }

        List<Long> longIds = companyIds.stream().toList();

        if (longIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Specification<LeadCompany> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("id").in(longIds));
            predicates.add(cb.equal(root.get("active"), true));

            // Apply data pack gate
            predicates.addAll(dataPackGate.buildGatePredicates(root, cb, gatedInfo, vendorAccess));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<LeadCompany> companiesPage = leadCompanyRepository.findAll(spec, pageable);
        List<LeadCompany> companies = companiesPage.getContent();

        if (companies.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> pageCompanyIds = companies.stream()
                .map(LeadCompany::getId)
                .toList();

        Map<Long, Long> contactCountMap = getContactCountsByCompanyIds(pageCompanyIds, gatedInfo, vendorAccess);

        List<LeadCompanyWithContactCountDto> content = companies.stream()
                .map(company -> {
                    long contactCount = contactCountMap.getOrDefault(company.getId(), 0L);
                    return LeadCompanyWithContactCountDto.fromEntity(company, contactCount);
                })
                .toList();

        return new PageImpl<>(content, pageable, companiesPage.getTotalElements());
    }
}
