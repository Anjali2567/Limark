package ai.leadplus.application.leadcompany;

import ai.leadplus.application.common.Recency;
import ai.leadplus.application.common.utils.RegionMapping;
import ai.leadplus.application.common.utils.StringNormalizer;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.leadcompanies.EmployeeRange;
import ai.leadplus.domain.leadcompanies.LeadCompany;
import ai.leadplus.domain.leadcompanies.LeadCompanyRepository;
import ai.leadplus.domain.leadcompanies.LeadCompanyStatus;
import ai.leadplus.domain.leadcontacts.CompanyContactCount;
import ai.leadplus.domain.leadcontacts.LeadContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LeadCompanyService {

    private final LeadCompanyRepository leadCompanyRepository;
    private final LeadContactRepository leadContactRepository;
    private final LeadCompanySearchService leadCompanySearchService;
    private final ApplicationEventPublisher eventPublisher;

    public LeadCompanyDto createCompany(LeadCompanyDto leadCompanyDto) {
        if (!StringUtils.hasText(leadCompanyDto.getDomain())) {
            throw new BadRequestException("Domain cannot be empty or null.");
        }

        LeadCompany savedCompany = leadCompanyRepository
                .findByDomainAndActiveTrue(leadCompanyDto.getDomain())
                .map(existing -> updateExistingCompany(existing, leadCompanyDto))
                .orElseGet(() -> {
                    LeadCompany leadCompany = createNewCompany(leadCompanyDto);
                    LeadCompanyCreatedEvent event = new LeadCompanyCreatedEvent(this, LeadCompanyDto.toDto(leadCompany));
                    eventPublisher.publishEvent(event);
                    return leadCompany;
                });

        return LeadCompanyDto.toDto(savedCompany);
    }

    public Page<LeadCompanyDto> getAllCompaniesByUpdatedAt(LocalDateTime updatedAt, Pageable pageable) {
        return leadCompanyRepository.findAllByUpdatedAtAfterAndActiveTrue(updatedAt, pageable)
                .map(LeadCompanyDto::toDto);
    }

    private LeadCompany updateExistingCompany(LeadCompany existing, LeadCompanyDto dto) {
        LeadCompany updated = updateEntityFromDto(dto, existing);
        return leadCompanyRepository.save(updated);
    }

    private LeadCompany createNewCompany(LeadCompanyDto dto) {
        LeadCompany newCompany = dto.toEntity();

        newCompany.setActive(true);
        newCompany.setRegion(getRegionByCityOrStateOrCountry(dto.getHqCity(), dto.getHqState(), dto.getHqCountry()));
        newCompany.setLeadCompanyStatus(LeadCompanyStatus.COLD);

        return leadCompanyRepository.save(newCompany);
    }

    public Page<LeadCompanyDto> searchCompanies(String searchText, List<String> segments, Pageable pageable) {
        return leadCompanySearchService.searchLeadCompanies(searchText, segments, pageable)
                .map(LeadCompanyDto::toDto);
    }

    public LeadCompanyDto getCompanyById(Long id) {
        return LeadCompanyDto.toDto(leadCompanyRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id)));
    }

    public LeadCompanyDto getCompanyByIdOrDomain(String id) {
        return LeadCompanyDto.toDto(findCompanyByIdOrDomain(id));
    }

    public List<LeadCompanyDto> getLeadCompaniesByIds(List<Long> ids) {
        List<LeadCompany> leadCompanies = leadCompanyRepository.findAllByIdInAndActiveTrue(ids);
        return leadCompanies.stream()
                .map(LeadCompanyDto::toDto)
                .toList();
    }

    public LeadCompanyDto updateCompanyByIdOrDomain(String id, LeadCompanyDto leadCompanyDto) {
        if (!StringUtils.hasText(leadCompanyDto.getDomain())) {
            throw new BadRequestException("Domain cannot be empty or null.");
        }

        LeadCompany leadCompany = findCompanyByIdOrDomain(id);

        if (!Objects.equals(leadCompanyDto.getDomain(), leadCompany.getDomain())) {
            throw new BadRequestException("Domain modification is not permitted during update operations.");
        }

        updateEntityFromDto(leadCompanyDto, leadCompany);
        leadCompanyRepository.save(leadCompany);
        LeadCompanyDto updatedDto = LeadCompanyDto.toDto(leadCompany);
        LeadCompanyUpdatedEvent event = new LeadCompanyUpdatedEvent(this, updatedDto);
        eventPublisher.publishEvent(event);
        return updatedDto;
    }

    public Page<LeadCompanyDto> searchLeadCompaniesBySalespersonIds(List<String> salespersonIds,
                                                                    String searchText,
                                                                    List<String> segments,
                                                                    List<String> regions,
                                                                    EmployeeRange employeeRange,
                                                                    Recency recency,
                                                                    List<String> states,
                                                                    Pageable pageable) {
        Page<LeadCompany> leadCompanies = leadCompanySearchService.searchLeadCompanies(searchText, segments, salespersonIds, regions, employeeRange, recency, states, pageable);
        return leadCompanies.map(LeadCompanyDto::toDto);
    }

    public Page<LeadCompanyDto> searchLeadCompaniesForVicePresident(String searchText,
                                                                    EmployeeRange employeeRange,
                                                                    Recency recency,
                                                                    Boolean salesPersonAssigned,
                                                                    List<String> segments,
                                                                    List<String> states,
                                                                    List<String> cities,
                                                                    List<String> countries,
                                                                    List<String> regions,
                                                                    Pageable pageable) {
        return leadCompanySearchService.searchLeadCompanies(searchText, segments, null, regions, employeeRange, recency, salesPersonAssigned, states, cities, countries, pageable)
                .map(LeadCompanyDto::toDto);
    }

    public void deleteCompanyByIdOrDomain(String id) {
        LeadCompany leadCompany = findCompanyByIdOrDomain(id);
        leadCompany.setActive(false);
        leadCompanyRepository.save(leadCompany);
    }


    public LeadCompanyWithContactCountDto getCompanyWithContactCount(String idOrDomain) {

        LeadCompany company = findCompanyByIdOrDomain(idOrDomain);
        List<CompanyContactCount> counts =
                leadContactRepository.countContactsByCompanyIds(List.of(company.getId()));
        long contactCount = counts.isEmpty() ? 0L : counts.getFirst().getCount();

        return LeadCompanyWithContactCountDto.fromEntity(company, contactCount);
    }

    private LeadCompany findCompanyByIdOrDomain(String idOrDomain) {
        Optional<LeadCompany> company;
        try {
            company = leadCompanyRepository.findByIdAndActiveTrue(Long.parseLong(idOrDomain));
        } catch (NumberFormatException e) {
            company = leadCompanyRepository.findByDomainAndActiveTrue(idOrDomain);
        }
        return company.orElseThrow(() -> new ResourceNotFoundException("Company not found with id or domain: " + idOrDomain));
    }

    private LeadCompany updateEntityFromDto(LeadCompanyDto dto, LeadCompany entity) {
        entity.setName(dto.getName());
        entity.setWebsiteUrl(dto.getWebsiteUrl());
        entity.setLinkedinUrl(dto.getLinkedinUrl());
        entity.setTwitterUrl(dto.getTwitterUrl());
        entity.setFacebookUrl(dto.getFacebookUrl());
        entity.setIndustry(dto.getIndustry());
        entity.setRevenueUsd(dto.getRevenueUsd());
        entity.setEmployeeCount(dto.getEmployeeCount());
        entity.setEmployeeRange(EmployeeRange.fromString(dto.getEmployeeCount()));
        entity.setHqCity(dto.getHqCity());
        entity.setHqState(dto.getHqState());
        entity.setHqCountry(dto.getHqCountry());
        entity.setPostalCode(dto.getPostalCode());
        entity.setTerritory(dto.getTerritory());
        entity.setRegion(getRegionByCityOrStateOrCountry(dto.getHqCity(), dto.getHqState(), dto.getHqCountry()));
        entity.setIcpTag(dto.getIcpTag());
        entity.setSicCodes(dto.getSicCodes());
        entity.setNaicsCodes(dto.getNaicsCodes());
        entity.setKeywords(dto.getKeywords());
        entity.setTechnologies(dto.getTechnologies());
        entity.setPubliclyTradedSymbol(dto.getPubliclyTradedSymbol());
        entity.setScore(dto.getScore());
        entity.setAccountSummary(dto.getAccountSummary());
        entity.setSource(dto.getSource());
        entity.setSegments(dto.getSegments());
        entity.setTargetAccount(dto.isTargetAccount());
        return entity;
    }

    public String getRegionByCityOrStateOrCountry(String city, String state, String country) {
        return findRegion(state)
                .or(() -> findRegion(city))
                .or(() -> findRegion(country))
                .orElse(null);
    }

    private Optional<String> findRegion(String value) {
        if (!StringUtils.hasText(value)) return Optional.empty();

        String normalized = StringNormalizer.normalizeEachWord(value);
        return RegionMapping.SALES_REGIONS.entrySet().stream()
                .filter(entry -> entry.getValue().contains(normalized))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public void validateCompanyIds(List<Long> companyIds) {
        if (CollectionUtils.isEmpty(companyIds))
            return;
        List<Long> longIds = companyIds.stream().toList();
        long count = leadCompanyRepository.countAllByIdInAndActiveTrue(longIds);
        if (count != companyIds.size()) {
            throw new BadRequestException("Company Ids are not valid");
        }
    }

    public void validateCompanyId(Long contactId) {
        if (!leadCompanyRepository.existsByIdAndActiveTrue(contactId)) {
            throw new BadRequestException("Company ID is invalid");
        }
    }

    public LeadCompanyDto saveLeadCompany(LeadCompanyDto leadCompanyDto) {
        LeadCompany leadCompany = leadCompanyDto.toEntity();
        LeadCompany savedCompany = leadCompanyRepository.save(leadCompany);
        return LeadCompanyDto.toDto(savedCompany);
    }

    public List<String> getUniqueSegments() {
        return leadCompanyRepository.findDistinctSegments();
    }

    public List<String> getUniqueIndustries() {
        return leadCompanyRepository.findDistinctIndustries();
    }

    public List<LeadCompanyDto> findCompaniesNeedingScrape(List<Long> excludedCompanyIds, int limit) {
        List<Long> excludedLongIds = excludedCompanyIds.stream().toList();
        return leadCompanyRepository
                .findActiveCompaniesWithWebsiteUrlExcluding(excludedLongIds, PageRequest.of(0, limit))
                .stream()
                .map(LeadCompanyDto::toDto)
                .toList();
    }

    public void updateScrapedTech(Long companyId, List<String> technologies, List<String> tools, List<String> services) {
        leadCompanyRepository.updateScrapedTech(companyId, technologies, tools, services);
    }
}
