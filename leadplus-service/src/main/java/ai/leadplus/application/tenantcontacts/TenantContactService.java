package ai.leadplus.application.tenantcontacts;

import ai.leadplus.domain.common.CRM;
import ai.leadplus.domain.tenantcompanies.TenantCompanyRepository;
import ai.leadplus.domain.tenantcontacts.TenantContact;
import ai.leadplus.domain.tenantcontacts.TenantContactRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantContactService {

    private final TenantContactRepository tenantContactRepository;
    private final TenantCompanyRepository tenantCompanyRepository;

    public void saveAll(Long tenantId, CRM sourceType, List<TenantContactPayload> payloads) {
        Set<String> seen = new HashSet<>();

        List<TenantContactPayload> uniquePayloads = payloads.stream()
                .filter(p -> StringUtils.hasText(p.getEmail()))
                .filter(p -> seen.add(p.getEmail().toLowerCase()))
                .toList();

        List<String> emails = uniquePayloads.stream()
                .map(p -> p.getEmail().toLowerCase())
                .toList();

        Set<String> existingEmails = tenantContactRepository
                .findAllByTenantIdAndEmailIn(tenantId, emails)
                .stream()
                .map(TenantContact::getEmail)
                .collect(Collectors.toSet());

        List<TenantContact> newContacts = payloads.stream()
                .filter(p -> StringUtils.hasText(p.getEmail()) && !existingEmails.contains(p.getEmail()))
                .map(p -> TenantContact.builder()
                        .tenantId(tenantId)
                        .sourceType(sourceType)
                        .sourceId(p.getSourceId())
                        .email(p.getEmail())
                        .firstName(p.getFirstName())
                        .lastName(p.getLastName())
                        .companyId(p.getCompanyId())
                        .build())
                .toList();

        tenantContactRepository.saveAll(newContacts);
        log.info("Saved {} new {} contacts for tenant {}", newContacts.size(), sourceType, tenantId);
    }

    public Set<String> getExistingEmails(Long tenantId, CRM sourceType, List<String> emails) {
        return tenantContactRepository
                .findAllByTenantIdAndSourceTypeAndEmailIn(tenantId, sourceType, emails)
                .stream()
                .map(TenantContact::getEmail)
                .collect(Collectors.toSet());
    }

    public Set<String> getExistingEmails(Long tenantId, List<String> emails) {
        return tenantContactRepository
                .findAllByTenantIdAndEmailIn(tenantId, emails)
                .stream()
                .map(TenantContact::getEmail)
                .collect(Collectors.toSet());
    }

    public List<TenantContactDto> getTenantContactsByIds(Long tenantId, List<Long> ids) {
        return tenantContactRepository.findAllByTenantIdAndIdIn(tenantId, ids).stream()
                .map(TenantContactDto::fromEntity)
                .toList();
    }

    public Page<TenantContactDto> searchContacts(Long tenantId, String searchQuery, Pageable pageable) {

        Specification<TenantContact> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));

            if (StringUtils.hasText(searchQuery)) {
                String[] words = searchQuery.trim().split("\\s+");
                for (String word : words) {
                    String pattern = "%" + word.toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("firstName")), pattern),
                            cb.like(cb.lower(root.get("lastName")), pattern)
                    ));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TenantContact> contactPage = tenantContactRepository.findAll(spec, pageable);
        List<TenantContact> contacts = contactPage.getContent();

        Map<CRM, List<String>> companyIdsBySourceType = contacts.stream()
                .filter(c -> c.getCompanyId() != null)
                .collect(Collectors.groupingBy(
                        TenantContact::getSourceType,
                        Collectors.mapping(TenantContact::getCompanyId, Collectors.toSet())
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new ArrayList<>(e.getValue())
                ));

        Map<String, String> companyNameMap = new HashMap<>();

        companyIdsBySourceType.forEach((sourceType, companyIds) -> {
            tenantCompanyRepository
                    .findAllByTenantIdAndSourceTypeAndSourceIdIn(tenantId, sourceType, companyIds)
                    .forEach(company ->
                            companyNameMap.put(sourceType + ":" + company.getSourceId(), company.getName())
                    );
        });

        List<TenantContactDto> dtoList = contacts.stream()
                .map(contact -> {
                    TenantContactDto dto = TenantContactDto.fromEntity(contact);
                    if (contact.getCompanyId() != null) {
                        dto.setCompanyName(companyNameMap.get(contact.getSourceType() + ":" + contact.getCompanyId()));
                    }
                    return dto;
                })
                .toList();

        return new PageImpl<>(dtoList, pageable, contactPage.getTotalElements());
    }
}
