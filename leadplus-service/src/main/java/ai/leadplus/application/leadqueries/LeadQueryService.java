package ai.leadplus.application.leadqueries;

import ai.leadplus.application.leadcompany.LeadCompanyCreatedEvent;
import ai.leadplus.application.leadcompany.LeadCompanyDto;
import ai.leadplus.application.leadcompany.LeadCompanyUpdatedEvent;
import ai.leadplus.application.leadcontact.LeadContactCreatedEvent;
import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.application.leadcontact.LeadContactUpdatedEvent;
import ai.leadplus.application.leadcontactnormalizedtitle.LeadContactNormalizedTitleDto;
import ai.leadplus.application.leadcontactnormalizedtitle.LeadContactNormalizedTitleService;
import ai.leadplus.application.leads.LeadFilterCriteria;
import ai.leadplus.domain.leadcontactnormalizedtitle.TitleAbbreviation;
import ai.leadplus.domain.leadqueries.LeadQuery;
import ai.leadplus.domain.leadqueries.LeadQueryRepository;
import ai.leadplus.domain.leadqueries.LeadQueryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadQueryService {

    private final LeadQueryRepository leadQueryRepository;
    private final LeadQuerySearchService leadQuerySearchService;
    private final LeadContactNormalizedTitleService leadContactNormalizedTitleService;

    public synchronized void saveLeadQuery(String value, LeadQueryType type) {
        if (!StringUtils.hasText(value) || type == null) {
            log.warn("Invalid lead query parameters: value='{}', type='{}'. Skipping save.", value, type);
            return;
        }

        String normalizedValue = value.trim();

        boolean exists = leadQueryRepository.existsByTypeAndValueIgnoreCase(type, normalizedValue);
        if (exists) {
            log.info("Lead query already exists: type='{}', value='{}'. Skipping save.", type, normalizedValue);
            return;
        }

        LeadQuery leadQuery = LeadQuery.builder()
                .type(type)
                .value(normalizedValue)
                .build();
        leadQueryRepository.save(leadQuery);
        log.info("Saved new lead query: type='{}', value='{}'.", type, normalizedValue);
    }

    public Page<LeadQueryDto> searchLeadQueries(String searchText, List<LeadQueryType> types, Pageable pageable) {
        Page<LeadQuery> leadQueryPage = leadQuerySearchService.searchLeadQueries(searchText, types, pageable);
        return leadQueryPage.map(LeadQueryDto::fromEntity);
    }

    public List<LeadQueryDto> getLeadQueriesByType(LeadQueryType type) {
        List<LeadQuery> leadQueries = leadQueryRepository.findAllByType(type);
        return leadQueries.stream()
                .map(LeadQueryDto::fromEntity)
                .toList();
    }

    public LeadFilterCriteria mapToLeadFilterCriteria(List<String> titles, List<String> seniorityList) {
        LeadFilterCriteria requestDto = new LeadFilterCriteria();

        if (!CollectionUtils.isEmpty(titles)) {
            List<String> uniqueTitles = searchAndCollectValues(titles, LeadQueryType.CONTACT_TITLE);
            requestDto.setTitles(uniqueTitles);
        }

        if (!CollectionUtils.isEmpty(seniorityList)) {
            List<String> foundSeniorities = searchAndCollectValues(seniorityList, LeadQueryType.CONTACT_SENIORITY);
            requestDto.setSeniority(foundSeniorities);
        }

        return requestDto;
    }

    private List<String> searchAndCollectValues(List<String> searchTerms, LeadQueryType type) {
        List<String> foundValues = new ArrayList<>();

        for (String searchTerm : searchTerms) {
            Page<LeadQueryDto> results = searchLeadQueries(
                    searchTerm,
                    List.of(type),
                    PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "value"))
            );
            results.forEach(dto -> foundValues.add(dto.getValue()));
        }

        return foundValues.stream()
                .distinct()
                .toList();
    }

    @Async
    @EventListener
    public void handleLeadCompanyCreatedEvent(LeadCompanyCreatedEvent event) {
        LeadCompanyDto leadCompanyDto = event.getLeadCompanyDto();
        updateQueriesFromCompany(leadCompanyDto);
    }

    @Async
    @EventListener
    public void handleLeadCompanyUpdatedEvent(LeadCompanyUpdatedEvent event) {
        LeadCompanyDto leadCompanyDto = event.getLeadCompanyDto();
        updateQueriesFromCompany(leadCompanyDto);
    }

    private void updateQueriesFromCompany(LeadCompanyDto leadCompanyDto) {
        if (leadCompanyDto == null) {
            return;
        }
        if (!CollectionUtils.isEmpty(leadCompanyDto.getSegments())) {
            leadCompanyDto.getSegments().forEach(s -> saveLeadQuery(s, LeadQueryType.COMPANY_INDUSTRY));
        }
        if (StringUtils.hasText(leadCompanyDto.getRegion())) {
            saveLeadQuery(leadCompanyDto.getRegion(), LeadQueryType.COMPANY_REGION);
        }
        if (StringUtils.hasText(leadCompanyDto.getHqCity())) {
            saveLeadQuery(leadCompanyDto.getHqCity(), LeadQueryType.COMPANY_CITY);
        }
        if (StringUtils.hasText(leadCompanyDto.getHqState())) {
            saveLeadQuery(leadCompanyDto.getHqState(), LeadQueryType.COMPANY_STATE);
        }
        if (StringUtils.hasText(leadCompanyDto.getHqCountry())) {
            saveLeadQuery(leadCompanyDto.getHqCountry(), LeadQueryType.COMPANY_COUNTRY);
        }
    }

    @Async
    @EventListener
    public void handleLeadContactCreatedEvent(LeadContactCreatedEvent event) {
        LeadContactDto leadContactDto = event.getLeadContactDto();
        updateQueriesFromContact(leadContactDto);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLeadContactUpdatedEvent(LeadContactUpdatedEvent event) {
        LeadContactDto leadContactDto = event.getLeadContactDto();
        updateQueriesFromContact(leadContactDto);
    }

    private void updateQueriesFromContact(LeadContactDto leadContactDto) {
        if (leadContactDto == null) {
            return;
        }
        if (StringUtils.hasText(leadContactDto.getSeniority())) {
            saveLeadQuery(leadContactDto.getSeniority(), LeadQueryType.CONTACT_SENIORITY);
        }
        if (StringUtils.hasText(leadContactDto.getTitle())) {
            LeadContactNormalizedTitleDto leadContactNormalizedTitleDto = leadContactNormalizedTitleService.getLeadContactNormalizedTitle(leadContactDto.getId());
            Set<String> tokens = new HashSet<>();

            if (leadContactNormalizedTitleDto.getKeywords() != null) {
                tokens.addAll(leadContactNormalizedTitleDto.getKeywords());
            }

            if (leadContactNormalizedTitleDto.getTitleAbbreviations() != null) {
                leadContactNormalizedTitleDto.getTitleAbbreviations().stream()
                        .map(TitleAbbreviation::getShortForm)
                        .filter(Objects::nonNull)
                        .forEach(tokens::add);
            }

            List<String> titles = new ArrayList<>(tokens);

            for (String title : titles) {
                saveLeadQuery(title, LeadQueryType.CONTACT_TITLE);
            }
        }
    }
}