package ai.leadplus.application.leadcontact;

import ai.leadplus.application.common.utils.StringNormalizer;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.DuplicateResourceException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.leadcompany.LeadCompanyDto;
import ai.leadplus.application.leadcompany.LeadCompanyService;
import ai.leadplus.application.leadcontactnormalizedtitle.LeadContactNormalizedTitleDto;
import ai.leadplus.application.leadcontactnormalizedtitle.LeadContactNormalizedTitleService;
import ai.leadplus.application.leads.LeadDto;
import ai.leadplus.domain.leadcontactnormalizedtitle.TitleAbbreviation;
import ai.leadplus.domain.leadcontacts.CompanyContactCount;
import ai.leadplus.domain.leadcontacts.LeadContact;
import ai.leadplus.domain.leadcontacts.LeadContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadContactService {

    private final LeadContactRepository leadContactRepository;
    private final LeadCompanyService leadCompanyService;
    private final LeadContactSearchService leadContactSearchService;
    private final ApplicationEventPublisher eventPublisher;
    private final LeadContactNormalizedTitleService leadContactNormalizedTitleService;

    @Transactional
    public LeadContactDto createContact(String companyIdOrDomain, LeadContactDto leadContactDto) {
        LeadCompanyDto leadCompanyDto = getLeadCompany(companyIdOrDomain);

        String normalizedFirst = StringNormalizer.normalize(leadContactDto.getFirstName());
        String normalizedLast = StringNormalizer.normalize(leadContactDto.getLastName());
        String fullName = LeadContactMapper.getContactFullName(leadContactDto.getFirstName(), leadContactDto.getLastName());

        Optional<LeadContact> existingOpt = leadContactRepository
                .findByLeadCompanyIdAndFullNameIgnoreCaseAndActiveTrue(leadCompanyDto.getId(), fullName);

        LeadContact contactToSave;

        if (existingOpt.isPresent()) {
            contactToSave = updateEntityFromDto(leadContactDto, existingOpt.get());
        } else {
            leadContactDto.setLeadCompanyId(leadCompanyDto.getId());
            leadContactDto.setActive(true);
            leadContactDto.setFirstNameNormalized(normalizedFirst);
            leadContactDto.setLastNameNormalized(normalizedLast);
            leadContactDto.setFullName(fullName);
            String email = toSimpleCase(leadContactDto.getEmail());
            leadContactDto.setEmail(email);

            contactToSave = leadContactDto.toEntity();
        }
        LeadContact saved = leadContactRepository.save(contactToSave);
        List<String> titles = updateContactTitles(LeadContactDto.toDto(saved));
        saved.setNormalizedTitleTokens(titles);
        saved = leadContactRepository.save(saved);
        LeadContactDto savedDto = LeadContactDto.toDto(saved);
        if (existingOpt.isEmpty()) {
            LeadContactCreatedEvent event = new LeadContactCreatedEvent(this, savedDto);
            eventPublisher.publishEvent(event);
        }
        return savedDto;
    }

    public Page<LeadContactDto> getAllContactsByUpdatedAt(LocalDateTime updatedAt, Pageable pageable) {
        return leadContactRepository.findAllByUpdatedAtAfterAndActiveTrue(updatedAt, pageable)
                .map(LeadContactDto::toDto);
    }

    public List<LeadContactDto> searchContacts(String searchText, String companyIdOrDomain) {
        LeadCompanyDto leadCompanyDto = getLeadCompany(companyIdOrDomain);
        List<LeadContact> leadContacts = leadContactSearchService.searchLeadContacts(searchText, leadCompanyDto.getId());
        return leadContacts.stream()
                .map(LeadContactDto::toDto)
                .toList();
    }

    public LeadContactDto getContactByIdAndCompanyIdOrDomain(String companyIdOrDomain, Long id) {
        LeadCompanyDto leadCompanyDto = getLeadCompany(companyIdOrDomain);
        return LeadContactDto.toDto(findContactById(leadCompanyDto.getId(), id));
    }

    public Page<LeadDto> getLeadByCompanyIdOrDomainAndContactId(String companyIdOrDomain, Pageable pageable) {
        LeadCompanyDto leadCompanyDto = getLeadCompany(companyIdOrDomain);
        Page<LeadContact> leadContact = findAllContactByCompanyId(leadCompanyDto.getId(), pageable);
        return leadContact.map(contact -> (LeadDto.fromEntities(contact, leadCompanyDto.toEntity())));
    }

    public LeadDto getLeadByContactId(Long contactId) {
        LeadContactDto leadContactDto = getContactById(contactId);
        LeadCompanyDto leadCompanyDto = leadCompanyService.getCompanyById(leadContactDto.getLeadCompanyId());
        return LeadDto.fromEntities(leadContactDto.toEntity(), leadCompanyDto.toEntity());
    }

    public LeadContactDto getContactById(Long id) {
        return LeadContactDto.toDto(findContactById(id));
    }

    private Page<LeadContact> findAllContactByCompanyId(Long companyId, Pageable pageable) {
        return leadContactRepository.findValidContacts(companyId, pageable);
    }

    private LeadContact findContactById(Long id) {
        return leadContactRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
    }

    public Map<Long, Long> getContactCounts(List<Long> companyIds) {
        List<CompanyContactCount> counts = leadContactRepository.countContactsByCompanyIds(companyIds);
        return counts.stream()
                .collect(Collectors.toMap(CompanyContactCount::getCompanyId, CompanyContactCount::getCount));
    }

    public LeadContactDto updateContact(String companyIdOrDomain, Long id, LeadContactDto leadContactDto) {
        LeadCompanyDto leadCompanyDto = getLeadCompany(companyIdOrDomain);
        LeadContact leadContact = findContactById(leadCompanyDto.getId(), id);

        String normalizedFirst = StringNormalizer.normalize(leadContactDto.getFirstName());
        String normalizedLast = StringNormalizer.normalize(leadContactDto.getLastName());
        String fullName = LeadContactMapper.getContactFullName(leadContactDto.getFirstName(), leadContactDto.getLastName());
        String title = leadContact.getTitle();

        if (!fullName.equals(leadContact.getFullName())
                && leadContactRepository.existsByLeadCompanyIdAndFullNameIgnoreCaseAndActiveTrue(leadCompanyDto.getId(), fullName)) {
            throw new DuplicateResourceException("Contact with same Full name already exists for company: " + companyIdOrDomain + " with name: " + fullName);
        }

        leadContact.setFirstNameNormalized(normalizedFirst);
        leadContact.setLastNameNormalized(normalizedLast);
        leadContact.setFullName(fullName);

        updateEntityFromDto(leadContactDto, leadContact);
        if (StringUtils.hasText(leadContactDto.getTitle())
                && !Objects.equals(title, leadContact.getTitle())) {
            leadContact.setNormalizedTitleTokens(updateContactTitles(leadContactDto));
        }
        leadContactRepository.save(leadContact);
        LeadContactDto updatedDto = LeadContactDto.toDto(leadContact);
        LeadContactUpdatedEvent leadContactUpdatedEvent = new LeadContactUpdatedEvent(this, updatedDto);
        eventPublisher.publishEvent(leadContactUpdatedEvent);
        return updatedDto;
    }

    public LeadContactDto updateNotes(Long id, Long companyId, String notes) {
        LeadContact leadContact = findContactById(companyId, id);
        leadContact.setNotes(notes);
        LeadContact savedContact = leadContactRepository.save(leadContact);
        return LeadContactDto.toDto(savedContact);
    }

    @Transactional
    public void deleteContact(String companyIdOrDomain, Long id) {
        LeadCompanyDto leadCompanyDto = getLeadCompany(companyIdOrDomain);
        LeadContact leadContact = findContactById(leadCompanyDto.getId(), id);
        leadContact.setActive(false);
        LeadContact savedLeadContact = leadContactRepository.save(leadContact);
        LeadContactDeletedEvent leadContactDeletedEvent = new LeadContactDeletedEvent(this, LeadContactDto.toDto(savedLeadContact));
        eventPublisher.publishEvent(leadContactDeletedEvent);
    }

    private LeadContact findContactById(Long companyId, Long id) {
        return leadContactRepository.findByIdAndLeadCompanyIdAndActiveTrue(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id + " for companyId: " + companyId));
    }


    private LeadCompanyDto getLeadCompany(String companyIdOrDomain) {
        return leadCompanyService.getCompanyByIdOrDomain(companyIdOrDomain);
    }

    public static String toSimpleCase(String input) {
        return (input != null) ? input.toLowerCase() : null;
    }

    private LeadContact updateEntityFromDto(LeadContactDto dto, LeadContact entity) {

        if (StringUtils.hasText(dto.getTitle())) entity.setTitle(dto.getTitle());
        if (StringUtils.hasText(dto.getSeniority())) entity.setSeniority(dto.getSeniority());
        if (StringUtils.hasText(dto.getDepartment())) entity.setDepartment(dto.getDepartment());
        if (StringUtils.hasText(dto.getEmail())) entity.setEmail(toSimpleCase(dto.getEmail()));
        if (StringUtils.hasText(dto.getEmailStatus())) entity.setEmailStatus(dto.getEmailStatus());
        if (StringUtils.hasText(dto.getPhoneE164())) entity.setPhoneE164(dto.getPhoneE164());
        if (StringUtils.hasText(dto.getLinkedinUrl())) entity.setLinkedinUrl(dto.getLinkedinUrl());
        if (StringUtils.hasText(dto.getLocationCity())) entity.setLocationCity(dto.getLocationCity());
        if (StringUtils.hasText(dto.getLocationState())) entity.setLocationState(dto.getLocationState());
        if (StringUtils.hasText(dto.getLocationCountry())) entity.setLocationCountry(dto.getLocationCountry());
        if (StringUtils.hasText(dto.getLocationZip())) entity.setLocationZip(dto.getLocationZip());
        if (StringUtils.hasText(dto.getPersonaMatch())) entity.setPersonaMatch(dto.getPersonaMatch());
        if (dto.getPersonaScore() != null) entity.setPersonaScore(dto.getPersonaScore());
        if (dto.getOwnerId() != null) entity.setOwnerId(dto.getOwnerId());
        if (StringUtils.hasText(dto.getConsentStatus())) entity.setConsentStatus(dto.getConsentStatus());
        entity.setDoNotContact(dto.isDoNotContact());
        if (StringUtils.hasText(dto.getSource())) entity.setSource(dto.getSource());
        if (StringUtils.hasText(dto.getNotes())) entity.setNotes(dto.getNotes());
        return entity;
    }

    public List<LeadContactDto> getLeadContactsByIds(List<Long> contactIds) {
        List<Long> longIds = contactIds.stream().toList();
        List<LeadContact> contacts = leadContactRepository.findAllByIdInAndActiveTrue(longIds);
        return contacts.stream()
                .map(LeadContactDto::toDto)
                .toList();
    }

    public Optional<LeadContactDto> getLeadCompanyIdAndFullName(Long companyId, String fullName) {
        Optional<LeadContact> contactOpt = leadContactRepository.findByLeadCompanyIdAndFullNameIgnoreCaseAndActiveTrue(companyId, fullName);
        return contactOpt.map(LeadContactDto::toDto);
    }

    public LeadContactDto saveLeadContact(LeadContactDto leadContactDto) {
        LeadContact leadContact = leadContactDto.toEntity();
        LeadContact savedContact = leadContactRepository.save(leadContact);
        return LeadContactDto.toDto(savedContact);
    }

    public List<LeadContactDto> getByLeadCompanyIds(List<Long> ids) {
        return leadContactRepository.findAllByLeadCompanyIdInAndActiveTrue(ids).stream()
                .map(LeadContactDto::toDto)
                .toList();
    }

    public void validateContactIds(List<Long> contactIds) {
        if (CollectionUtils.isEmpty(contactIds))
            return;

        List<Long> longIds = contactIds.stream().toList();

        if (longIds.size() != contactIds.size()) {
            throw new BadRequestException("One or more contact IDs are invalid");
        }

        long existingCount = leadContactRepository.countAllByIdInAndActiveTrue(longIds);
        if (existingCount != contactIds.size()) {
            throw new BadRequestException("One or more contact IDs are invalid");
        }
    }

    public void validateContactId(Long contactId) {
        if (!leadContactRepository.existsByIdAndActiveTrue(contactId)) {
            throw new BadRequestException("Contact ID is invalid");
        }
    }

    private List<String> updateContactTitles(LeadContactDto leadContactDto) {
        if (StringUtils.hasText(leadContactDto.getTitle())) {
            LeadContactNormalizedTitleDto titles = leadContactNormalizedTitleService.createOrUpdateLeadContactNormalizedTitle(leadContactDto);
            Set<String> tokens = new HashSet<>();

            if (titles.getKeywords() != null) {
                tokens.addAll(titles.getKeywords());
            }

            if (titles.getTitleAbbreviations() != null) {
                titles.getTitleAbbreviations().stream()
                        .map(TitleAbbreviation::getShortForm)
                        .filter(Objects::nonNull)
                        .forEach(tokens::add);
            }
            return new ArrayList<>(tokens);
        }
        return new ArrayList<>();
    }
}
