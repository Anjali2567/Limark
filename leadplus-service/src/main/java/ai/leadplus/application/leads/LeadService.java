package ai.leadplus.application.leads;

import ai.leadplus.application.datapacks.DataPackGate;
import ai.leadplus.application.leaddatapacks.GatedInfo;
import ai.leadplus.application.vendordatapacks.VendorAccess;
import ai.leadplus.domain.leadcompanies.LeadCompany;
import ai.leadplus.domain.leadcompanies.LeadCompanyRepository;
import ai.leadplus.domain.leadcontacts.LeadContact;
import ai.leadplus.domain.leadcontacts.LeadContactRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadContactRepository leadContactRepository;
    private final LeadCompanyRepository leadCompanyRepository;
    private final DataPackGate dataPackGate;

    @PersistenceContext
    private final EntityManager entityManager;

    public LeadStatisticsDto getStatistics() {
        CompletableFuture<Long> activeContactsFuture = CompletableFuture.supplyAsync(() ->
                leadContactRepository.count((root, query, cb) -> cb.equal(root.get("active"), true))
        );

        CompletableFuture<Long> activeCompaniesFuture = CompletableFuture.supplyAsync(() ->
                leadCompanyRepository.count((root, query, cb) -> cb.equal(root.get("active"), true))
        );

        CompletableFuture<Long> contactsWithEmailsFuture = CompletableFuture.supplyAsync(() ->
                leadContactRepository.count((root, query, cb) -> cb.and(
                        cb.equal(root.get("active"), true),
                        cb.isNotNull(root.get("email")),
                        cb.notEqual(root.get("email"), "")
                ))
        );

        CompletableFuture<Long> companiesWithPhoneFuture = CompletableFuture.supplyAsync(() ->
                leadCompanyRepository.count((root, query, cb) -> cb.and(
                        cb.equal(root.get("active"), true),
                        cb.isNotNull(root.get("phoneNumber")),
                        cb.notEqual(root.get("phoneNumber"), "")
                ))
        );

        CompletableFuture.allOf(
                activeContactsFuture,
                activeCompaniesFuture,
                contactsWithEmailsFuture,
                companiesWithPhoneFuture
        ).join();

        return LeadStatisticsDto.builder()
                .totalContacts(activeContactsFuture.join())
                .totalCompanies(activeCompaniesFuture.join())
                .totalEmails(contactsWithEmailsFuture.join())
                .totalPhoneNumbers(companiesWithPhoneFuture.join())
                .build();
    }

    public LeadStatisticsDto getGatedStatistics(Specification<LeadCompany> gateSpec,
                                                   GatedInfo gatedInfo,
                                                   VendorAccess vendorAccess) {
        if (gateSpec == null) {
            return getStatistics();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<LeadContact> contactRoot = query.from(LeadContact.class);

        // Subquery: IDs of active companies matching the gate spec
        Subquery<Long> companySubquery = query.subquery(Long.class);
        Root<LeadCompany> companyRoot = companySubquery.from(LeadCompany.class);
        companySubquery.select(companyRoot.get("id"));
        companySubquery.where(
                cb.and(
                        cb.equal(companyRoot.get("active"), true),
                        gateSpec.toPredicate(companyRoot, query, cb)
                )
        );

        // Contact-level predicates
        List<Predicate> contactPredicates = new ArrayList<>();
        contactPredicates.add(cb.equal(contactRoot.get("active"), true));
        contactPredicates.add(cb.isNotNull(contactRoot.get("email")));
        contactPredicates.add(cb.notEqual(contactRoot.get("email"), ""));
        contactPredicates.add(contactRoot.get("leadCompanyId").in(companySubquery));

        // Apply contact-level data pack gate
        contactPredicates.addAll(dataPackGate.buildGatePredicates(contactRoot, cb, gatedInfo, vendorAccess));

        // Aggregate contacts whose company is in the subquery
        query.multiselect(
                cb.countDistinct(contactRoot.get("leadCompanyId")),
                cb.count(contactRoot.get("id"))
        );
        query.where(cb.and(contactPredicates.toArray(new Predicate[0])));

        Object[] result = entityManager.createQuery(query).getSingleResult();
        long companiesWithContacts = ((Number) result[0]).longValue();
        long totalContacts = ((Number) result[1]).longValue();

        if (totalContacts == 0) {
            return emptyStats();
        }

        return LeadStatisticsDto.builder()
                .totalCompanies(companiesWithContacts)
                .totalContacts(totalContacts)
                .totalEmails(totalContacts)
                .totalPhoneNumbers(companiesWithContacts)
                .build();
    }

    private LeadStatisticsDto emptyStats() {
        return LeadStatisticsDto.builder()
                .totalCompanies(0).totalContacts(0L)
                .totalEmails(0L).totalPhoneNumbers(0).build();
    }
}
