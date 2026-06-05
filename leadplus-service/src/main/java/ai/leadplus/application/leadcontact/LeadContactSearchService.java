package ai.leadplus.application.leadcontact;

import ai.leadplus.domain.leadcompanies.LeadCompany;
import ai.leadplus.domain.leadcompanies.LeadCompanyRepository;
import ai.leadplus.domain.leadcontacts.LeadContact;
import ai.leadplus.domain.leadcontacts.LeadContactRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadContactSearchService {

    private final LeadContactRepository leadContactRepository;
    private final LeadCompanyRepository leadCompanyRepository;

    public Page<LeadContactListDto> searchAllLeads(String searchText, Pageable pageable) {
        Specification<LeadContact> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("active"), true));
            predicates.add(cb.isNotNull(root.get("email")));
            predicates.add(cb.notEqual(root.get("email"), ""));

            if (StringUtils.hasText(searchText)) {
                String[] words = searchText.trim().split("\\s+");
                for (String word : words) {
                    String pattern = "%" + word.toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("firstName")), pattern),
                            cb.like(cb.lower(root.get("lastName")), pattern),
                            cb.like(cb.lower(root.get("title")), pattern),
                            cb.like(cb.lower(root.get("phoneE164")), pattern),
                            cb.like(cb.lower(root.get("email")), pattern)
                    ));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<LeadContact> contactPage = leadContactRepository.findAll(spec, pageable);
        List<LeadContact> contacts = contactPage.getContent();

        if (contacts.isEmpty()) return Page.empty(pageable);

        Set<Long> companyIds = contacts.stream()
                .map(LeadContact::getLeadCompanyId)
                .collect(Collectors.toSet());

        List<LeadCompany> companies = leadCompanyRepository.findAllByIdInAndActiveTrue(companyIds.stream().toList());

        Map<Long, String> companyMap = companies.stream()
                .collect(Collectors.toMap(LeadCompany::getId, LeadCompany::getName));

        List<LeadContactListDto> dtos = contacts.stream()
                .map(contact -> LeadContactListDto.builder()
                        .id(contact.getId())
                        .firstName(contact.getFirstName())
                        .lastName(contact.getLastName())
                        .title(contact.getTitle())
                        .email(contact.getEmail())
                        .phoneE164(contact.getPhoneE164())
                        .companyName(companyMap.get(contact.getLeadCompanyId()))
                        .build())
                .toList();

        return new PageImpl<>(dtos, pageable, contactPage.getTotalElements());
    }

    public List<LeadContact> searchLeadContacts(String searchText, Long leadCompanyId) {
        Specification<LeadContact> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("active"), true));

            if (StringUtils.hasText(searchText)) {
                String[] words = searchText.trim().split("\\s+");
                for (String word : words) {
                    String pattern = "%" + word.toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("fullName")), pattern),
                            cb.like(cb.lower(root.get("email")), pattern),
                            cb.like(cb.lower(root.get("title")), pattern),
                            cb.like(cb.lower(root.get("personaMatch")), pattern)
                    ));
                }
            }

            if (leadCompanyId != null) {
                predicates.add(cb.equal(root.get("leadCompanyId"), leadCompanyId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return leadContactRepository.findAll(spec);
    }

    public List<LeadContact> searchContactsByCompaniesAndFilters(
            List<Long> companyIds,
            List<String> jobTitles,
            List<String> states,
            List<String> cities,
            List<String> countries) {

        Specification<LeadContact> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("active"), true));

            if (!CollectionUtils.isEmpty(companyIds)) {
                predicates.add(root.get("leadCompanyId").in(companyIds));
            }

            if (!CollectionUtils.isEmpty(jobTitles)) {
                List<Predicate> titlePredicates = new ArrayList<>();
                for (String jobTitle : jobTitles) {
                    if (!StringUtils.hasText(jobTitle)) continue;
                    String pattern = "%" + jobTitle.trim().toLowerCase() + "%";
                    titlePredicates.add(cb.like(cb.lower(root.get("title")), pattern));
                }
                if (!titlePredicates.isEmpty()) {
                    predicates.add(cb.or(titlePredicates.toArray(new Predicate[0])));
                }
            }

            if (!CollectionUtils.isEmpty(states)) {
                List<Predicate> statePredicates = new ArrayList<>();
                for (String state : states) {
                    String pattern = "%" + state.toLowerCase() + "%";
                    statePredicates.add(cb.like(cb.lower(root.get("locationState")), pattern));
                }
                predicates.add(cb.or(statePredicates.toArray(new Predicate[0])));
            }

            if (!CollectionUtils.isEmpty(cities)) {
                List<Predicate> cityPredicates = new ArrayList<>();
                for (String city : cities) {
                    String pattern = "%" + city.toLowerCase() + "%";
                    cityPredicates.add(cb.like(cb.lower(root.get("locationCity")), pattern));
                }
                predicates.add(cb.or(cityPredicates.toArray(new Predicate[0])));
            }

            if (!CollectionUtils.isEmpty(countries)) {
                List<Predicate> countryPredicates = new ArrayList<>();
                for (String country : countries) {
                    String pattern = "%" + country.toLowerCase() + "%";
                    countryPredicates.add(cb.like(cb.lower(root.get("locationCountry")), pattern));
                }
                predicates.add(cb.or(countryPredicates.toArray(new Predicate[0])));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return leadContactRepository.findAll(spec);
    }

    public List<Long> filterCompanyIdsWithContacts(Set<Long> companyIds) {
        if (companyIds.isEmpty()) {
            return List.of();
        }

        return leadContactRepository.findAllByLeadCompanyIdInAndActiveTrue(companyIds.stream().toList())
                .stream()
                .map(LeadContact::getLeadCompanyId)
                .distinct()
                .toList();
    }
}
