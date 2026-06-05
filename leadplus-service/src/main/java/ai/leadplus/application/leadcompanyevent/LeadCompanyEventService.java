package ai.leadplus.application.leadcompanyevent;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.leadcompany.LeadCompanyDto;
import ai.leadplus.application.leadcompany.LeadCompanyService;
import ai.leadplus.domain.leadcompanyevents.LeadCompanyEvent;
import ai.leadplus.domain.leadcompanyevents.LeadCompanyEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeadCompanyEventService {

    private final LeadCompanyEventRepository leadCompanyEventRepository;
    private final LeadCompanyService leadCompanyService;

    public LeadCompanyEventDto createEvent(String companyIdOrDomain, LeadCompanyEventDto eventDto) {
        LeadCompanyDto leadCompanyDto = getLeadCompany(companyIdOrDomain);
        eventDto.setLeadCompanyId(leadCompanyDto.getId());
        eventDto.setActive(true);
        
        if(eventDto.getDetectedAt() == null) {
            eventDto.setDetectedAt(LocalDateTime.now());
        }
        LeadCompanyEvent saved = leadCompanyEventRepository.save(eventDto.toEntity());
        return LeadCompanyEventDto.toDto(saved);
    }

    public List<LeadCompanyEventDto> getEventsByCompanyIdOrDomain(String companyIdOrDomain) {
        LeadCompanyDto leadCompanyDto = getLeadCompany(companyIdOrDomain);
        return getEventsByCompanyId(leadCompanyDto.getId());
    }

    public List<LeadCompanyEventDto> getEventsByCompanyId(Long companyId) {
        return leadCompanyEventRepository.findAllByLeadCompanyIdAndActiveTrue(companyId)
                .stream()
                .map(LeadCompanyEventDto::toDto)
                .toList();
    }

    public LeadCompanyEventDto getEventById(String companyIdOrDomain, Long eventId) {
        LeadCompanyDto leadCompanyDto = getLeadCompany(companyIdOrDomain);
        return LeadCompanyEventDto.toDto(findEventByCompanyIdAndId(eventId, leadCompanyDto.getId()));
    }

    public LeadCompanyEventDto updateEvent(String companyIdOrDomain, Long eventId, LeadCompanyEventDto eventDto) {
        LeadCompanyDto leadCompanyDto = getLeadCompany(companyIdOrDomain);
        LeadCompanyEvent existingEvent = findEventByCompanyIdAndId(eventId, leadCompanyDto.getId());
        LocalDateTime detectedAt = eventDto.getDetectedAt() != null ? eventDto.getDetectedAt() : existingEvent.getDetectedAt();

        existingEvent.setType(eventDto.getType());
        existingEvent.setTitle(eventDto.getTitle());
        existingEvent.setSummary(eventDto.getSummary());
        existingEvent.setUrl(eventDto.getUrl());
        existingEvent.setSource(eventDto.getSource());
        existingEvent.setSentiment(eventDto.getSentiment());
        existingEvent.setDetectedAt(detectedAt);
        existingEvent.setUniqueHash(eventDto.getUniqueHash());
        existingEvent.setPublishedAt(eventDto.getPublishedAt());

        LeadCompanyEvent updated = leadCompanyEventRepository.save(existingEvent);
        return LeadCompanyEventDto.toDto(updated);
    }

    public void deleteEvent(String companyIdOrDomain, Long eventId) {
        LeadCompanyDto leadCompanyDto = getLeadCompany(companyIdOrDomain);
        LeadCompanyEvent existingEvent = findEventByCompanyIdAndId(eventId, leadCompanyDto.getId());
        existingEvent.setActive(false);
        leadCompanyEventRepository.save(existingEvent);
    }

    private LeadCompanyEvent findEventByCompanyIdAndId(Long eventId, Long companyId) {
        return leadCompanyEventRepository.findByIdAndLeadCompanyIdAndActiveTrue(eventId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId + " for companyId: " + companyId));
    }

    private LeadCompanyDto getLeadCompany(String companyIdOrDomain) {
        return leadCompanyService.getCompanyByIdOrDomain(companyIdOrDomain);
    }
}

