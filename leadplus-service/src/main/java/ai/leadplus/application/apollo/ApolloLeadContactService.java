package ai.leadplus.application.apollo;

import ai.leadplus.application.apollocompanydata.ApolloCompanyDataDto;
import ai.leadplus.application.apollocontactdata.ApolloContactDataDto;
import ai.leadplus.application.apollocontactdata.ApolloContactDataService;
import ai.leadplus.application.exception.InvalidJsonFormatException;
import ai.leadplus.application.leadcompany.ApolloDataFetchedEvent;
import ai.leadplus.application.leadcompany.LeadCompanyDto;
import ai.leadplus.application.leadcontact.LeadContactCreatedEvent;
import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.application.leadcontact.LeadContactMapper;
import ai.leadplus.application.leadcontact.LeadContactService;
import ai.leadplus.domain.common.ApolloDataType;
import ai.leadplus.infrastructure.apollo.enrichment.ApolloEnrichmentClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "apollo.enabled", havingValue = "true")
public class ApolloLeadContactService {

    private final ObjectMapper objectMapper;
    private final LeadContactService leadContactService;
    private final ApolloEnrichmentClient apolloEnrichmentClient;
    private final ApolloContactDataService apolloContactDataService;
    private final ApplicationEventPublisher eventPublisher;

    public void enrichContactWithApolloData(Long id) {
        LeadContactDto leadContact = leadContactService.getContactById(id);
        if (StringUtils.hasText(leadContact.getEmail()) || !StringUtils.hasText(leadContact.getApolloId()) || leadContact.isApolloEnriched()) {
            return;
        }
        if (Objects.equals(leadContact.getEmailStatus(), "verified")) {
            String apolloId = leadContact.getApolloId();
            String enrichedData = apolloEnrichmentClient.enrichByApolloId(apolloId);
            try {
                JsonNode enrichedNode = objectMapper.readTree(enrichedData);
                JsonNode personNode = enrichedNode.get("person");
                if (personNode != null) {
                    updateApolloDataFromNode(personNode, leadContact, ApolloDataType.SINGLE_ENRICHMENT);
                }
            } catch (JsonProcessingException e) {
                throw new InvalidJsonFormatException("Failed to parse Apollo data for contact enrichment");
            }
        }
    }

    public void enrichContactWithApolloData(List<Long> contactIds) {
        List<LeadContactDto> contacts = leadContactService.getLeadContactsByIds(contactIds);
        Map<String, LeadContactDto> apolloIdToContactMap = contacts.stream()
                .filter(contact -> StringUtils.hasText(contact.getApolloId())
                        && !StringUtils.hasText(contact.getEmail())
                        && !contact.isApolloEnriched()
                        && Objects.equals(contact.getEmailStatus(), "verified"))
                .collect(Collectors.toMap(LeadContactDto::getApolloId, contact -> contact));

        if (CollectionUtils.isEmpty(apolloIdToContactMap)) {
            return;
        }

        String enrichedData = apolloEnrichmentClient.bulkEnrichByApolloId(List.copyOf(apolloIdToContactMap.keySet()));
        try {
            JsonNode enrichedNode = objectMapper.readTree(enrichedData);
            JsonNode matchesArray = enrichedNode.get("matches");
            if (matchesArray != null && matchesArray.isArray()) {
                for (JsonNode matchNode : matchesArray) {
                    String apolloId = matchNode.has("id") ? matchNode.get("id").asText() : null;
                    LeadContactDto leadContact = apolloIdToContactMap.get(apolloId);
                    if (leadContact != null) {
                        updateApolloDataFromNode(matchNode, leadContact, ApolloDataType.BULK_ENRICHMENT);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            throw new InvalidJsonFormatException("Failed to parse Apollo data for contact enrichment");
        }

    }

    private void updateApolloDataFromNode(JsonNode rootNode, LeadContactDto leadContact, ApolloDataType type) {
        LeadContactMapper.mapFromApolloPerson(leadContact, rootNode);
        ApolloContactDataDto apolloContactDataDto = ApolloContactDataDto.builder()
                .leadContactId(leadContact.getId())
                .type(type)
                .data(rootNode.toString())
                .fetchedAt(LocalDateTime.now())
                .build();
        leadContact.setApolloEnriched(true);
        apolloContactDataService.createApolloContactData(apolloContactDataDto);
        leadContactService.saveLeadContact(leadContact);
        log.info("Enriched contact with id: {} with email: {} from Apollo", leadContact.getId(), leadContact.getEmail());
    }

    @Async
    @EventListener
    public void handleApolloDataFetchedEvent(ApolloDataFetchedEvent event) {
        ApolloCompanyDataDto apolloCompanyDataDto = event.getApolloCompanyDataDto();
        if (!StringUtils.hasText(apolloCompanyDataDto.getData())) {
            return;
        }

        LeadCompanyDto leadCompanyDto = event.getLeadCompanyDto();
        String apolloData = apolloCompanyDataDto.getData();
        Map<LeadContactDto, JsonNode> contactToPersonNodeMap = parseApolloData(apolloData);

        for (Map.Entry<LeadContactDto, JsonNode> entry : contactToPersonNodeMap.entrySet()) {
            LeadContactDto leadContact = entry.getKey();
            JsonNode personNode = entry.getValue();
            String fullName = leadContact.getFullName();

            Optional<LeadContactDto> existingOpt = leadContactService
                     .getLeadCompanyIdAndFullName(leadCompanyDto.getId(), fullName);

            LeadContactDto savedContact;
            if (existingOpt.isEmpty()) {
                leadContact.setLeadCompanyId(leadCompanyDto.getId());
                String email = LeadContactService.toSimpleCase(leadContact.getEmail());
                leadContact.setEmail(email);
                savedContact = leadContactService.saveLeadContact(leadContact);
                log.info("Imported contact: {} for company: {}", fullName, leadCompanyDto.getName());
                LeadContactCreatedEvent contactCreatedEvent = new LeadContactCreatedEvent(this, savedContact);
                eventPublisher.publishEvent(contactCreatedEvent);
            } else {
                LeadContactDto existing = existingOpt.get();
                existing.setApolloId(leadContact.getApolloId());
                savedContact = leadContactService.saveLeadContact(existing);
                log.info("Contact: {} for company: {} already exists. Skipping import.", fullName, leadCompanyDto.getName());
            }
            saveApolloContactData(savedContact, personNode, apolloCompanyDataDto);
        }
    }

    private Map<LeadContactDto, JsonNode> parseApolloData(String apolloData) {
        Map<LeadContactDto, JsonNode> contactToPersonNodeMap = new HashMap<>();

        try {
            JsonNode rootNode = objectMapper.readTree(apolloData);
            JsonNode peopleArray = rootNode.get("people");

            if (peopleArray != null && peopleArray.isArray()) {
                for (JsonNode personNode : peopleArray) {
                    LeadContactDto leadContact = LeadContactMapper.mapFromApolloPerson(personNode);
                    contactToPersonNodeMap.put(leadContact, personNode);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Apollo data", e);
            throw new InvalidJsonFormatException("Failed to extract lead contacts: " + e.getMessage());
        }

        return contactToPersonNodeMap;
    }

    private void saveApolloContactData(LeadContactDto savedContact, JsonNode personNode, ApolloCompanyDataDto apolloCompanyDataDto) {
        ApolloContactDataDto apolloContactDataDto = ApolloContactDataDto.builder()
                .leadContactId(savedContact.getId())
                .type(apolloCompanyDataDto.getType())
                .specificationId(apolloCompanyDataDto.getSpecificationId())
                .data(personNode.toString())
                .fetchedAt(apolloCompanyDataDto.getFetchedAt())
                .build();
        apolloContactDataService.createApolloContactData(apolloContactDataDto);
    }
}
