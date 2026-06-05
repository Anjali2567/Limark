package ai.leadplus.application.leadcompany;

import ai.leadplus.api.common.datetime.TimeZoneContext;
import ai.leadplus.application.common.Recency;
import ai.leadplus.application.datapacks.DataPackGate;
import ai.leadplus.application.leaddatapacks.GatedInfo;
import ai.leadplus.application.vendordatapacks.VendorAccess;
import ai.leadplus.domain.leadcompanies.EmployeeRange;
import ai.leadplus.domain.leadcompanies.LeadCompany;
import ai.leadplus.domain.leadcompanies.LeadCompanyRepository;
import ai.leadplus.domain.leadcontacts.CompanyContactCount;
import ai.leadplus.domain.leadcontacts.LeadContactRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadCompanySearchService {

    private final LeadCompanyRepository leadCompanyRepository;
    private final LeadContactRepository leadContactRepository;
    private final DataPackGate dataPackGate;

    public Page<LeadCompany> searchLeadCompanies(String searchText,
                                                 List<String> segments,
                                                 Pageable pageable) {
        return searchLeadCompanies(searchText, segments, null, null, null, null, null, null, null, null, pageable);
    }

    public LeadCompanyWithCountDto searchLeadCompaniesWithCounts(String searchText,
                                                       List<String> segments,
                                                       Pageable pageable) {
        return searchLeadCompaniesWithCount(searchText, segments, null, null, null, null, null, null, null, null, pageable);
    }

    public Page<LeadCompany> searchLeadCompanies(String searchText,
                                                 List<String> segments,
                                                 List<String> salespersonIds,
                                                 List<String> regions,
                                                 EmployeeRange employeeRange,
                                                 Recency recency,
                                                 List<String> states,
                                                 Pageable pageable) {
        return searchLeadCompanies(searchText, segments, salespersonIds, regions, employeeRange, recency, null, states, null, null, pageable);
    }

    public Page<LeadCompany> searchLeadCompanies(String searchText,
                                                 List<String> segments,
                                                 List<String> salespersonIds,
                                                 List<String> regions,
                                                 EmployeeRange employeeRange,
                                                 Recency recency,
                                                 Boolean salesPersonAssigned,
                                                 List<String> states,
                                                 List<String> cities,
                                                 List<String> countries,
                                                 Pageable pageable) {
        Specification<LeadCompany> spec = buildSearchSpecification(searchText, segments, salespersonIds, regions, employeeRange, recency, salesPersonAssigned, states, cities, countries);
        return leadCompanyRepository.findAll(spec, pageable);
    }

    public LeadCompanyWithCountDto searchLeadCompaniesWithCount(String searchText,
                                                                List<String> segments,
                                                                List<String> salespersonIds,
                                                                List<String> regions,
                                                                EmployeeRange employeeRange,
                                                                Recency recency,
                                                                Boolean salesPersonAssigned,
                                                                List<String> states,
                                                                List<String> cities,
                                                                List<String> countries,
                                                                Pageable pageable) {

        Specification<LeadCompany> spec = buildSearchSpecification(searchText, segments, salespersonIds, regions,
                employeeRange, recency, salesPersonAssigned,
                states, cities, countries);

        Page<LeadCompany> paginatedCompaniesPage = leadCompanyRepository.findAll(spec, pageable);
        List<LeadCompany> paginatedCompanies = paginatedCompaniesPage.getContent();
        long totalCompanies = paginatedCompaniesPage.getTotalElements();

        if (totalCompanies == 0) {
            return LeadCompanyWithCountDto.builder()
                    .companies(new PageImpl<>(Collections.emptyList(), pageable, 0))
                    .companiesCount(0)
                    .contactsCount(0)
                    .build();
        }

        // For total counts across all matches, we might need a separate way if totalCompanies is large
        // But the original code was loading all IDs. Let's try to get all IDs from the spec.
        // Optimization: if we just need contact count, we might need a join or a separate count query.
        
        // Following original logic of getting all matching IDs:
        List<Long> allCompanyIds = leadCompanyRepository.findAll(spec).stream()
                .map(LeadCompany::getId)
                .collect(Collectors.toList());

        List<CompanyContactCount> counts = leadContactRepository.countContactsByCompanyIds(allCompanyIds);
        Map<Long, Long> allContactCountMap = counts.stream()
                .collect(Collectors.toMap(CompanyContactCount::getCompanyId, CompanyContactCount::getCount));

        int totalContacts = allContactCountMap.values().stream()
                .mapToInt(Long::intValue)
                .sum();

        List<LeadCompanyWithContactCountDto> dtoList = paginatedCompanies.stream()
                .map(company -> LeadCompanyWithContactCountDto.fromEntity(
                        company,
                        allContactCountMap.getOrDefault(company.getId(), 0L)))
                .collect(Collectors.toList());

        Page<LeadCompanyWithContactCountDto> page = new PageImpl<>(dtoList, pageable, totalCompanies);

        return LeadCompanyWithCountDto.builder()
                .companies(page)
                .companiesCount((int) totalCompanies)
                .contactsCount(totalContacts)
                .build();
    }

    private Specification<LeadCompany> buildSearchSpecification(String searchText,
                                         List<String> segments,
                                         List<String> salespersonIds,
                                         List<String> regions,
                                         EmployeeRange employeeRange,
                                         Recency recency,
                                         Boolean salesPersonAssigned,
                                         List<String> states,
                                         List<String> cities,
                                         List<String> countries) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(searchText)) {
                String[] searchWords = searchText.trim().split("\\s+");
                for (String word : searchWords) {
                    String pattern = "%" + word.toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("name")), pattern),
                            cb.like(cb.lower(root.get("domain")), pattern),
                            cb.like(cb.lower(root.get("industry")), pattern),
                            cb.like(cb.lower(root.get("accountSummary")), pattern)
                    ));
                }
            }

            if (!CollectionUtils.isEmpty(segments)) {
                predicates.add(arrayContainsAny(cb, root.get("segments"), segments));
            }

            if (!CollectionUtils.isEmpty(salespersonIds)) {
                predicates.add(root.get("salespersonId").in(salespersonIds));
            }

            if (!CollectionUtils.isEmpty(regions)) {
                predicates.add(caseInsensitiveIn(cb, root.get("region"), regions));
            }

            if (employeeRange != null) {
                predicates.add(cb.equal(root.get("employeeRange"), employeeRange));
            }

            if (salesPersonAssigned != null) {
                if (salesPersonAssigned) {
                    predicates.add(cb.isNotNull(root.get("salespersonId")));
                } else {
                    predicates.add(cb.isNull(root.get("salespersonId")));
                }
            }

            if (recency != null) {
                LocalDate today = LocalDate.now(TimeZoneContext.getTimeZone());
                LocalDateTime fromDateTime = switch (recency) {
                    case THIS_WEEK -> today.with(ChronoField.DAY_OF_WEEK, 1)
                            .atStartOfDay();
                    case THIS_MONTH -> today.withDayOfMonth(1)
                            .atStartOfDay();
                    case THIS_YEAR -> today.withDayOfYear(1)
                            .atStartOfDay();
                };
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
            }

            if (!CollectionUtils.isEmpty(states)) {
                predicates.add(caseInsensitiveIn(cb, root.get("hqState"), states));
            }

            if (!CollectionUtils.isEmpty(cities)) {
                predicates.add(caseInsensitiveIn(cb, root.get("hqCity"), cities));
            }

            if (!CollectionUtils.isEmpty(countries)) {
                predicates.add(caseInsensitiveIn(cb, root.get("hqCountry"), countries));
            }

            predicates.add(cb.equal(root.get("active"), true));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Predicate caseInsensitiveIn(CriteriaBuilder cb, jakarta.persistence.criteria.Expression<String> expression, List<String> values) {
        return cb.or(values.stream()
                .map(v -> cb.equal(cb.lower(expression), v.toLowerCase()))
                .toArray(Predicate[]::new));
    }

    public List<LeadCompanyDto> searchLeadCompanies(
            List<String> companyNames,
            List<String> segments,
            List<EmployeeRange> employeeRanges,
            List<String> states,
            List<String> cities,
            List<String> countries,
            List<String> regions) {

        Specification<LeadCompany> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("active"), true));

            if (!CollectionUtils.isEmpty(companyNames)) {
                List<Predicate> namePredicates = new ArrayList<>();
                for (String name : companyNames) {
                    String pattern = "%" + name.toLowerCase() + "%";
                    namePredicates.add(cb.like(cb.lower(root.get("name")), pattern));
                }
                predicates.add(cb.or(namePredicates.toArray(new Predicate[0])));
            }

            if (!CollectionUtils.isEmpty(segments)) {
                predicates.add(arrayContainsAny(cb, root.get("segments"), segments));
            }

            if (!CollectionUtils.isEmpty(employeeRanges)) {
                predicates.add(root.get("employeeRange").in(employeeRanges));
            }

            if (!CollectionUtils.isEmpty(states)) {
                predicates.add(caseInsensitiveIn(cb, root.get("hqState"), states));
            }

            if (!CollectionUtils.isEmpty(cities)) {
                predicates.add(caseInsensitiveIn(cb, root.get("hqCity"), cities));
            }

            if (!CollectionUtils.isEmpty(countries)) {
                predicates.add(caseInsensitiveIn(cb, root.get("hqCountry"), countries));
            }

            if (!CollectionUtils.isEmpty(regions)) {
                predicates.add(caseInsensitiveIn(cb, root.get("region"), regions));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return leadCompanyRepository.findAll(spec, Pageable.ofSize(20)).getContent().stream()
                .map(LeadCompanyDto::toDto)
                .toList();
    }

    public List<LeadCompanyDto> searchLeadCompaniesWithExclusion(
            List<String> excludeSegments,
            List<EmployeeRange> employeeRanges,
            List<String> excludeStates,
            List<String> excludeCities,
            List<String> excludeCountries,
            List<String> excludeRegions) {

        Specification<LeadCompany> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("active"), true));

            if (!CollectionUtils.isEmpty(employeeRanges)) {
                predicates.add(root.get("employeeRange").in(employeeRanges));
            }

            if (!CollectionUtils.isEmpty(excludeSegments)) {
                predicates.add(cb.not(arrayContainsAny(cb, root.get("segments"), excludeSegments)));
            }

            if (!CollectionUtils.isEmpty(excludeStates)) {
                predicates.add(cb.not(root.get("hqState").in(excludeStates)));
            }

            if (!CollectionUtils.isEmpty(excludeCities)) {
                predicates.add(cb.not(root.get("hqCity").in(excludeCities)));
            }

            if (!CollectionUtils.isEmpty(excludeCountries)) {
                predicates.add(cb.not(root.get("hqCountry").in(excludeCountries)));
            }

            if (!CollectionUtils.isEmpty(excludeRegions)) {
                predicates.add(cb.not(root.get("region").in(excludeRegions)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return leadCompanyRepository.findAll(spec, Pageable.ofSize(20)).getContent().stream()
                .map(LeadCompanyDto::toDto)
                .toList();
    }

    public List<Long> filterCompanyIdsByGate(List<Long> companyIds, GatedInfo gatedInfo, VendorAccess vendorAccess) {
        if (CollectionUtils.isEmpty(companyIds)) return companyIds;

        Specification<LeadCompany> gateSpec = buildDataPackGateSpecification(gatedInfo, vendorAccess);
        
        Specification<LeadCompany> combinedSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("id").in(companyIds));
            predicates.add(cb.equal(root.get("active"), true));
            if (gateSpec != null) {
                predicates.add(gateSpec.toPredicate(root, query, cb));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return leadCompanyRepository.findAll(combinedSpec).stream()
                .map(LeadCompany::getId)
                .toList();
    }

    public boolean isCompanyInaccessible(
            List<String> segments,
            List<String> tenantIds,
            GatedInfo gatedInfo,
            VendorAccess vendorAccess
    ) {
        return !dataPackGate.isAccessible(segments, tenantIds, gatedInfo, vendorAccess);
    }

    public Specification<LeadCompany> buildDataPackGateSpecification(GatedInfo gatedInfo, VendorAccess vendorAccess) {
        return dataPackGate.buildGateSpecification(gatedInfo, vendorAccess);
    }

    private Predicate arrayContainsAny(
            CriteriaBuilder cb,
            Expression<List<String>> arrayExpr,
            List<String> values
    ) {
        List<Predicate> preds = new ArrayList<>();

        for (String v : values) {
            preds.add(cb.isTrue(
                    cb.function(
                            "array_contains",
                            Boolean.class,
                            arrayExpr,
                            cb.literal(v)
                    )
            ));
        }

        return cb.or(preds.toArray(new Predicate[0]));
    }
}