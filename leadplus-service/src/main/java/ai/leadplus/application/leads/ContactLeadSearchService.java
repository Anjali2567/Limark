package ai.leadplus.application.leads;

import ai.leadplus.application.campaigns.CampaignDto;
import ai.leadplus.application.campaigns.CampaignService;
import ai.leadplus.application.contactoutreachstatuses.ContactOutreachStatusDto;
import ai.leadplus.application.contactoutreachstatuses.ContactOutreachStatusService;
import ai.leadplus.application.datapacks.DataPackGate;
import ai.leadplus.application.leadcompany.LeadCompanySearchService;
import ai.leadplus.application.leaddatapacks.GatedInfo;
import ai.leadplus.application.tenantcontacts.TenantContactService;
import ai.leadplus.application.vendordatapacks.VendorAccess;
import ai.leadplus.domain.common.CRM;
import ai.leadplus.domain.leadcompanies.LeadCompany;
import ai.leadplus.domain.leadcompanies.LeadCompanyRepository;
import ai.leadplus.domain.leadcontacts.LeadContact;
import ai.leadplus.domain.leadcontacts.LeadContactRepository;
import ai.leadplus.domain.tenantcontactmetadata.TenantContactMetadata;
import ai.leadplus.domain.tenantcontactmetadata.TenantContactMetadataRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactLeadSearchService {

    private static final int MAX_COMPANY_IDS = 20_000;
    private static final int MAX_ID_RESULTS = 25_000;

    private final LeadContactRepository leadContactRepository;
    private final LeadCompanyRepository leadCompanyRepository;
    private final TenantContactMetadataRepository tenantContactMetadataRepository;
    private final ContactOutreachStatusService contactOutreachStatusService;
    private final CampaignService campaignService;
    private final TenantContactService tenantContactService;
    private final LeadCompanySearchService leadCompanySearchService;
    private final DataPackGate dataPackGate;
    private final EntityManager entityManager;

    public Page<LeadDto> searchLeads(LeadFilterCriteria req, Pageable pageable) {
        return searchLeads(null, req, null, null, pageable);
    }

    public Page<LeadDto> searchLeads(Long tenantId, LeadFilterCriteria req, String query, List<Long> companyIds, Pageable pageable) {

        List<Long> effectiveCompanyIds = companyIds;
        if (CollectionUtils.isEmpty(effectiveCompanyIds)) {
            if (req.hasCompanyFilters()) {
                effectiveCompanyIds = findMatchingCompanyIds(req, null);

                if (effectiveCompanyIds.isEmpty()) {
                    return Page.empty(pageable);
                }

                if (effectiveCompanyIds.size() >= MAX_COMPANY_IDS) {
                    log.warn("Company filter matched {} companies, results may be incomplete. Consider narrowing filters.", effectiveCompanyIds.size());
                }
            }
        }

        Page<LeadContact> contactPage = findContacts(req, query, effectiveCompanyIds, tenantId, null, null, pageable);

        if (contactPage.isEmpty()) {
            return Page.empty(pageable);
        }

        Set<Long> companyIdsInPage = contactPage.getContent().stream()
                .map(LeadContact::getLeadCompanyId)
                .collect(Collectors.toSet());

        Map<Long, LeadCompany> companyMap = companyIdsInPage.isEmpty()
                ? Collections.emptyMap()
                : fetchCompaniesById(new ArrayList<>(companyIdsInPage));

        List<LeadDto> leads = contactPage.getContent().stream()
                .map(contact -> LeadDto.fromEntities(contact, companyMap.get(contact.getLeadCompanyId())))
                .toList();

        if (tenantId != null) {
            enrichWithTenantData(tenantId, leads);
        }

        return new PageImpl<>(leads, pageable, contactPage.getTotalElements());
    }

    public Page<LeadDto> searchLeads(Long tenantId, LeadFilterCriteria req, String query,
                                     List<Long> companyIds, GatedInfo gatedInfo, VendorAccess vendorAccess, Pageable pageable) {

        List<Long> effectiveCompanyIds = companyIds;

        if (gatedInfo != null) {
            if (!CollectionUtils.isEmpty(effectiveCompanyIds)) {
                effectiveCompanyIds = leadCompanySearchService.filterCompanyIdsByGate(effectiveCompanyIds, gatedInfo, vendorAccess);
                if (effectiveCompanyIds.isEmpty()) return Page.empty(pageable);
            } else {
                Specification<LeadCompany> gateSpec = leadCompanySearchService.buildDataPackGateSpecification(gatedInfo, vendorAccess);
                if (gateSpec != null) {
                    if (req.hasCompanyFilters()) {
                        effectiveCompanyIds = findMatchingCompanyIds(req, null);
                        if (!CollectionUtils.isEmpty(effectiveCompanyIds)) {
                            effectiveCompanyIds = leadCompanySearchService.filterCompanyIdsByGate(effectiveCompanyIds, gatedInfo, vendorAccess);
                        }
                    } else {
                        effectiveCompanyIds = findMatchingCompanyIds(req, gateSpec);
                    }
                    if (CollectionUtils.isEmpty(effectiveCompanyIds)) return Page.empty(pageable);
                }
            }
        }

        if (CollectionUtils.isEmpty(effectiveCompanyIds) && req.hasCompanyFilters()) {
            effectiveCompanyIds = findMatchingCompanyIds(req, null);
            if (effectiveCompanyIds.isEmpty()) return Page.empty(pageable);
        }

        Page<LeadContact> contactPage = findContacts(req, query, effectiveCompanyIds, tenantId, gatedInfo, vendorAccess, pageable);

        if (contactPage.isEmpty()) {
            return Page.empty(pageable);
        }

        Set<Long> companyIdsInPage = contactPage.getContent().stream()
                .map(LeadContact::getLeadCompanyId)
                .collect(Collectors.toSet());

        Map<Long, LeadCompany> companyMap = companyIdsInPage.isEmpty()
                ? Collections.emptyMap()
                : fetchCompaniesById(new ArrayList<>(companyIdsInPage));

        List<LeadDto> leads = contactPage.getContent().stream()
                .map(contact -> LeadDto.fromEntities(contact, companyMap.get(contact.getLeadCompanyId())))
                .toList();

        if (tenantId != null) {
            enrichWithTenantData(tenantId, leads);
        }

        return new PageImpl<>(leads, pageable, contactPage.getTotalElements());
    }

    public Page<LeadDto> searchLeadsByIds(Long tenantId, List<Long> contactIds, Pageable pageable) {
        if (CollectionUtils.isEmpty(contactIds)) {
            return Page.empty(pageable);
        }

        List<Long> ids = contactIds.stream().toList();

        if (ids.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<LeadContact> contactPage = leadContactRepository.findAllByIdInAndActiveTrue(ids, pageable);

        if (contactPage.isEmpty()) {
            return Page.empty(pageable);
        }

        Set<Long> companyIdsInPage = contactPage.getContent().stream()
                .map(LeadContact::getLeadCompanyId)
                .collect(Collectors.toSet());

        Map<Long, LeadCompany> companyMap = companyIdsInPage.isEmpty()
                ? Collections.emptyMap()
                : fetchCompaniesById(new ArrayList<>(companyIdsInPage));

        List<LeadDto> leads = contactPage.getContent().stream()
                .map(contact -> LeadDto.fromEntities(contact, companyMap.get(contact.getLeadCompanyId())))
                .toList();

        if (tenantId != null) {
            enrichWithTenantData(tenantId, leads);
        }

        return new PageImpl<>(leads, pageable, contactPage.getTotalElements());
    }

    private List<Long> findMatchingCompanyIds(LeadFilterCriteria req, Specification<LeadCompany> gateSpec) {
        Specification<LeadCompany> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("active"), true));

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

            if (!CollectionUtils.isEmpty(req.getRevenueRanges())) {
                List<Predicate> revenuePreds = new ArrayList<>();
                for (String range : req.getRevenueRanges()) {
                    if (range.endsWith("+")) {
                        try {
                            double min = Double.parseDouble(range.substring(0, range.length() - 1).trim());
                            revenuePreds.add(cb.greaterThanOrEqualTo(root.get("revenueUsdAmount"), min));
                        } catch (NumberFormatException ignored) {}
                    } else if (range.contains("-")) {
                        try {
                            String[] parts = range.split("-");
                            double min = Double.parseDouble(parts[0].trim());
                            double max = Double.parseDouble(parts[1].trim());
                            revenuePreds.add(cb.between(root.get("revenueUsdAmount"), min, max));
                        } catch (NumberFormatException ignored) {}
                    }
                }
                if (!revenuePreds.isEmpty()) {
                    predicates.add(cb.or(revenuePreds.toArray(new Predicate[0])));
                }
            }

            if (!CollectionUtils.isEmpty(req.getRegions())) {
                predicates.add(cb.or(req.getRegions().stream()
                        .map(reg -> cb.equal(cb.lower(root.get("region")), reg.toLowerCase()))
                        .toArray(Predicate[]::new)));
            }

            List<String> locationSearchValues = new ArrayList<>();
            if (!CollectionUtils.isEmpty(req.getCompanyCities())) locationSearchValues.addAll(req.getCompanyCities());
            if (!CollectionUtils.isEmpty(req.getCompanyStates())) locationSearchValues.addAll(req.getCompanyStates());
            if (!CollectionUtils.isEmpty(req.getCompanyCountries())) locationSearchValues.addAll(req.getCompanyCountries());

            if (!locationSearchValues.isEmpty()) {
                List<String> lowerLocations = locationSearchValues.stream().map(String::toLowerCase).toList();
                predicates.add(cb.or(
                        cb.lower(root.get("hqCity")).in(lowerLocations),
                        cb.lower(root.get("hqState")).in(lowerLocations),
                        cb.lower(root.get("hqCountry")).in(lowerLocations)
                ));
            }

            if (!CollectionUtils.isEmpty(req.getPostalCodes())) {
                predicates.add(root.get("postalCode").in(req.getPostalCodes()));
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

            if (gateSpec != null) {
                Predicate gatePredicate = gateSpec.toPredicate(root, query, cb);
                if (gatePredicate != null) {
                    predicates.add(gatePredicate);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return leadCompanyRepository.findAll(spec, Pageable.ofSize(MAX_COMPANY_IDS)).getContent().stream()
                .map(LeadCompany::getId)
                .toList();
    }

    public List<Long> findContactIds(LeadFilterCriteria req, String searchQuery, List<Long> companyIds,
                                     Long tenantId, GatedInfo gatedInfo, VendorAccess vendorAccess) {
        List<Long> effectiveCompanyIds = companyIds;

        if (gatedInfo != null) {
            if (!CollectionUtils.isEmpty(effectiveCompanyIds)) {
                effectiveCompanyIds = leadCompanySearchService.filterCompanyIdsByGate(effectiveCompanyIds, gatedInfo, vendorAccess);
                if (effectiveCompanyIds.isEmpty()) return Collections.emptyList();
            } else {
                Specification<LeadCompany> gateSpec = leadCompanySearchService.buildDataPackGateSpecification(gatedInfo, vendorAccess);
                if (gateSpec != null) {
                    if (req.hasCompanyFilters()) {
                        effectiveCompanyIds = findMatchingCompanyIds(req, null);
                        if (!CollectionUtils.isEmpty(effectiveCompanyIds)) {
                            effectiveCompanyIds = leadCompanySearchService.filterCompanyIdsByGate(effectiveCompanyIds, gatedInfo, vendorAccess);
                        }
                    } else {
                        effectiveCompanyIds = findMatchingCompanyIds(req, gateSpec);
                    }
                    if (CollectionUtils.isEmpty(effectiveCompanyIds)) return Collections.emptyList();
                }
            }
        }

        if (CollectionUtils.isEmpty(effectiveCompanyIds) && req.hasCompanyFilters()) {
            effectiveCompanyIds = findMatchingCompanyIds(req, null);
            if (effectiveCompanyIds.isEmpty()) return Collections.emptyList();
        }

        Specification<LeadContact> spec = buildContactSpecification(req, searchQuery, effectiveCompanyIds, tenantId, gatedInfo, vendorAccess);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<LeadContact> root = cq.from(LeadContact.class);
        cq.select(root.get("id"));

        Predicate predicate = spec.toPredicate(root, cq, cb);
        if (predicate != null) {
            cq.where(predicate);
        }

        return entityManager.createQuery(cq)
                .setMaxResults(MAX_ID_RESULTS)
                .getResultList();
    }

    public long countMatchingCompanies(LeadFilterCriteria req, String searchQuery, List<Long> companyIds,
                                       Long tenantId, GatedInfo gatedInfo, VendorAccess vendorAccess) {
        List<Long> effectiveCompanyIds = companyIds;

        if (gatedInfo != null) {
            if (!CollectionUtils.isEmpty(effectiveCompanyIds)) {
                effectiveCompanyIds = leadCompanySearchService.filterCompanyIdsByGate(effectiveCompanyIds, gatedInfo, vendorAccess);
                if (effectiveCompanyIds.isEmpty()) return 0L;
            } else {
                Specification<LeadCompany> gateSpec = leadCompanySearchService.buildDataPackGateSpecification(gatedInfo, vendorAccess);
                if (gateSpec != null) {
                    if (req.hasCompanyFilters()) {
                        effectiveCompanyIds = findMatchingCompanyIds(req, null);
                        if (!CollectionUtils.isEmpty(effectiveCompanyIds)) {
                            effectiveCompanyIds = leadCompanySearchService.filterCompanyIdsByGate(effectiveCompanyIds, gatedInfo, vendorAccess);
                        }
                    } else {
                        effectiveCompanyIds = findMatchingCompanyIds(req, gateSpec);
                    }
                    if (CollectionUtils.isEmpty(effectiveCompanyIds)) return 0L;
                }
            }
        }

        if (CollectionUtils.isEmpty(effectiveCompanyIds) && req.hasCompanyFilters()) {
            effectiveCompanyIds = findMatchingCompanyIds(req, null);
            if (effectiveCompanyIds.isEmpty()) return 0L;
        }

        Specification<LeadContact> spec = buildContactSpecification(req, searchQuery, effectiveCompanyIds, tenantId, gatedInfo, vendorAccess);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<LeadContact> root = cq.from(LeadContact.class);
        cq.select(cb.countDistinct(root.get("leadCompanyId")));

        Predicate predicate = spec.toPredicate(root, cq, cb);
        if (predicate != null) {
            cq.where(predicate);
        }

        Long result = entityManager.createQuery(cq).getSingleResult();
        return result == null ? 0L : result;
    }

    private Page<LeadContact> findContacts(LeadFilterCriteria req, String searchQuery, List<Long> companyIds,
                                           Long tenantId, GatedInfo gatedInfo, VendorAccess vendorAccess,
                                           Pageable pageable) {
        Specification<LeadContact> spec = buildContactSpecification(req, searchQuery, companyIds, tenantId, gatedInfo, vendorAccess);
        return leadContactRepository.findAll(spec, pageable);
    }

    private Specification<LeadContact> buildContactSpecification(LeadFilterCriteria req, String searchQuery,
                                                                  List<Long> companyIds, Long tenantId,
                                                                  GatedInfo gatedInfo, VendorAccess vendorAccess) {
        return (root, query, cb) -> {
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

            if (!CollectionUtils.isEmpty(companyIds)) {
                predicates.add(root.get("leadCompanyId").in(companyIds));
            }

            if (StringUtils.hasText(searchQuery)) {
                predicates.add(buildNamePredicate(cb, root, searchQuery));
            }

            if (!CollectionUtils.isEmpty(req.getContactNames())) {
                List<Predicate> namePreds = req.getContactNames().stream()
                        .map(name -> buildNamePredicate(cb, root, name))
                        .toList();
                predicates.add(cb.or(namePreds.toArray(new Predicate[0])));
            }

            List<String> locationSearchValues = new ArrayList<>();
            if (!CollectionUtils.isEmpty(req.getCities())) locationSearchValues.addAll(req.getCities());
            if (!CollectionUtils.isEmpty(req.getStates())) locationSearchValues.addAll(req.getStates());
            if (!CollectionUtils.isEmpty(req.getCountries())) locationSearchValues.addAll(req.getCountries());

            if (!locationSearchValues.isEmpty()) {
                List<String> lowerLocations = locationSearchValues.stream().map(String::toLowerCase).toList();
                predicates.add(cb.or(
                        cb.lower(root.get("locationCity")).in(lowerLocations),
                        cb.lower(root.get("locationState")).in(lowerLocations),
                        cb.lower(root.get("locationCountry")).in(lowerLocations)
                ));
            }

            if (!CollectionUtils.isEmpty(req.getTitles())) {
                List<Predicate> titlePreds = new ArrayList<>();
                for (String title : req.getTitles()) {
                    String pattern = "%" + title.toLowerCase() + "%";
                    titlePreds.add(cb.or(
                            cb.like(cb.lower(root.get("title")), pattern),
                            cb.like(cb.lower(cb.function("array_to_string", String.class, root.get("normalizedTitleTokens"), cb.literal(","))), pattern)
                    ));
                }
                predicates.add(cb.or(titlePreds.toArray(new Predicate[0])));
            }

            if (!CollectionUtils.isEmpty(req.getSeniority())) {
                predicates.add(cb.or(req.getSeniority().stream()
                        .map(s -> cb.equal(cb.lower(root.get("seniority")), s.toLowerCase()))
                        .toArray(Predicate[]::new)));
            }

            if (!CollectionUtils.isEmpty(req.getDepartments())) {
                List<Predicate> deptPreds = new ArrayList<>();
                for (String dept : req.getDepartments()) {
                    String normalizedDept = normalizeSearchText(dept);
                    if (!StringUtils.hasText(normalizedDept)) {
                        continue;
                    }

                    deptPreds.add(cb.like(normalizeDepartmentExpression(cb, root), "%" + normalizedDept + "%"));
                }
                if (!deptPreds.isEmpty()) {
                    predicates.add(cb.or(deptPreds.toArray(new Predicate[0])));
                }
            }

            if (!CollectionUtils.isEmpty(req.getContactSegments())) {
                List<Predicate> segPreds = new ArrayList<>();
                for (String seg : req.getContactSegments()) {
                    segPreds.add(cb.isTrue(
                            cb.function("array_contains", Boolean.class, root.get("segments"), cb.literal(seg))
                    ));
                }
                predicates.add(cb.or(segPreds.toArray(new Predicate[0])));
            }

            if (tenantId != null && req.hasTenantMetadataFilters()) {
                Subquery<Long> tcmSubquery = query.subquery(Long.class);
                Root<TenantContactMetadata> tcmRoot = tcmSubquery.from(TenantContactMetadata.class);
                tcmSubquery.select(tcmRoot.get("leadContactId"));

                List<Predicate> tcmPredicates = new ArrayList<>();
                tcmPredicates.add(cb.equal(tcmRoot.get("tenantId"), tenantId));
                tcmPredicates.add(cb.equal(tcmRoot.get("leadContactId"), root.get("id")));

                if (!CollectionUtils.isEmpty(req.getBdNames())) {
                    tcmPredicates.add(cb.lower(tcmRoot.get("bdName")).in(
                            req.getBdNames().stream().map(String::toLowerCase).toList()));
                }
                if (!CollectionUtils.isEmpty(req.getIsrNames())) {
                    tcmPredicates.add(cb.lower(tcmRoot.get("isrName")).in(
                            req.getIsrNames().stream().map(String::toLowerCase).toList()));
                }
                if (!CollectionUtils.isEmpty(req.getPriorities())) {
                    tcmPredicates.add(cb.lower(tcmRoot.get("priority")).in(
                            req.getPriorities().stream().map(String::toLowerCase).toList()));
                }
                if (!CollectionUtils.isEmpty(req.getTitleCategories())) {
                    tcmPredicates.add(cb.lower(tcmRoot.get("titleCategory")).in(
                            req.getTitleCategories().stream().map(String::toLowerCase).toList()));
                }

                tcmSubquery.where(cb.and(tcmPredicates.toArray(new Predicate[0])));
                predicates.add(cb.exists(tcmSubquery));
            }

            if (!CollectionUtils.isEmpty(req.getExcludeContactIds())) {
                List<Long> excludeIds = req.getExcludeContactIds().stream().toList();
                if (!excludeIds.isEmpty()) {
                    predicates.add(cb.not(root.get("id").in(excludeIds)));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Predicate buildNamePredicate(CriteriaBuilder cb, Root<LeadContact> root, String input) {
        String[] words = input.trim().split("\\s+");
        List<Predicate> wordPredicates = new ArrayList<>();
        for (String word : words) {
            String pattern = "%" + word.toLowerCase() + "%";
            wordPredicates.add(cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern)
            ));
        }
        return cb.and(wordPredicates.toArray(new Predicate[0]));
    }

    private Expression<String> normalizeDepartmentExpression(CriteriaBuilder cb, Root<LeadContact> root) {
        Expression<String> normalized = cb.lower(cb.trim(root.get("department")));
        normalized = cb.function("replace", String.class, normalized, cb.literal("_"), cb.literal(" "));
        normalized = cb.function("replace", String.class, normalized, cb.literal("-"), cb.literal(" "));
        return normalized;
    }

    private String normalizeSearchText(String input) {
        if (!StringUtils.hasText(input)) {
            return null;
        }

        return input.trim().toLowerCase(Locale.ROOT).replaceAll("[\\s_\\-]+", " ");
    }

    private Map<Long, LeadCompany> fetchCompaniesById(List<Long> companyIds) {
        List<Long> ids = companyIds.stream().toList();
        if (ids.isEmpty()) return Collections.emptyMap();
        List<LeadCompany> companies = leadCompanyRepository.findAllByIdInAndActiveTrue(ids);
        return companies.stream().collect(Collectors.toMap(LeadCompany::getId, c -> c));
    }

    private void enrichWithTenantData(Long tenantId, List<LeadDto> leads) {
        if (leads.isEmpty()) {
            return;
        }

        List<Long> contactIds = leads.stream()
                .map(LeadDto::getId)
                .toList();

        List<String> emails = leads.stream()
                .map(LeadDto::getEmail)
                .filter(StringUtils::hasText)
                .toList();

        // Fetch outreach status, zoho contacts, and hubspot contacts in parallel
        CompletableFuture<Map<Long, ContactOutreachStatusDto>> outreachFuture =
                CompletableFuture.supplyAsync(() ->
                        contactOutreachStatusService.getContactOutreachStatusDtoMap(tenantId, contactIds.stream().toList()));

        CompletableFuture<Set<String>> zohoFuture =
                CompletableFuture.supplyAsync(() ->
                        tenantContactService.getExistingEmails(tenantId, CRM.ZOHO, emails));

        CompletableFuture<Set<String>> hubspotFuture =
                CompletableFuture.supplyAsync(() ->
                        tenantContactService.getExistingEmails(tenantId, CRM.HUBSPOT, emails));

        CompletableFuture<Map<Long, TenantContactMetadata>> tcmFuture =
                CompletableFuture.supplyAsync(() -> {
                    List<TenantContactMetadata> tcmList = tenantContactMetadataRepository
                            .findAllByTenantIdAndLeadContactIdIn(tenantId, contactIds);
                    return tcmList.stream().collect(Collectors.toMap(TenantContactMetadata::getLeadContactId, t -> t));
                });

        CompletableFuture.allOf(outreachFuture, zohoFuture, hubspotFuture, tcmFuture).join();

        Map<Long, ContactOutreachStatusDto> outreachStatusMap = outreachFuture.join();
        Set<String> zohoExistingEmails = zohoFuture.join();
        Set<String> hubspotExistingEmails = hubspotFuture.join();
        Map<Long, TenantContactMetadata> tcmMap = tcmFuture.join();

        // Get all campaign IDs from outreach statuses
        Set<Long> allCampaignIds = outreachStatusMap.values().stream()
                .filter(status -> !CollectionUtils.isEmpty(status.getCurrentCampaignIds()))
                .flatMap(status -> status.getCurrentCampaignIds().stream())
                .collect(Collectors.toSet());

        Map<Long, CampaignDto> campaignMap = campaignService.getCampaignsByIds(allCampaignIds);

        // Enrich each lead
        leads.forEach(lead -> {
            // Set campaign data
            ContactOutreachStatusDto statusDto = outreachStatusMap.get(lead.getId());
            if (statusDto != null && !CollectionUtils.isEmpty(statusDto.getCurrentCampaignIds())) {
                List<CurrentCampaignDto> currentCampaigns = statusDto.getCurrentCampaignIds().stream()
                        .map(campaignId -> {
                            CampaignDto campaignDto = campaignMap.getOrDefault(campaignId, new CampaignDto());
                            return CurrentCampaignDto.builder()
                                    .id(campaignDto.getId())
                                    .name(campaignDto.getName())
                                    .status(campaignDto.getStatus())
                                    .build();
                        })
                        .toList();
                lead.setCurrentCampaigns(currentCampaigns);
            }

            // Set zoho and hubspot existing flags
            boolean zohoExisting = lead.getEmail() != null && zohoExistingEmails.contains(lead.getEmail());
            lead.setZohoExisting(zohoExisting);
            boolean hubspotExisting = lead.getEmail() != null && hubspotExistingEmails.contains(lead.getEmail());
            lead.setHubspotExisting(hubspotExisting);

            TenantContactMetadata tcm = tcmMap.get(lead.getId());
            if (tcm != null) {
                lead.setTenantMetadata(TenantContactMetadataDto.fromEntity(tcm));
            }
        });
    }
}
