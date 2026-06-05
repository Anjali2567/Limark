package ai.leadplus.application.facts;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.facts.Fact;
import ai.leadplus.domain.facts.FactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FactService {

    private final FactRepository factRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public FactDto addFact(FactDto factDto) {
        Fact fact = factRepository.save(factDto.toEntity());
        FactDto savedFactDto = FactDto.toDto(fact);

        FactCreatedEvent factCreatedEvent = new FactCreatedEvent(this, savedFactDto);
        eventPublisher.publishEvent(factCreatedEvent);

        return savedFactDto;
    }

    @Transactional
    public FactDto updateFact(Long id, FactDto factDto) {
        Fact fact = getFactById(id);
        fact.setFact(factDto.getFact());

        FactDto savedFactDto = FactDto.toDto(factRepository.save(fact));
        FactUpdateEvent factUpdateEvent = new FactUpdateEvent(this, savedFactDto);
        eventPublisher.publishEvent(factUpdateEvent);

        return savedFactDto;
    }

    @Transactional
    public void deleteFact(Long id) {
        Fact fact = getFactById(id);
        factRepository.delete(fact);

        FactDto savedFactDto = FactDto.toDto(fact);
        FactDeleteEvent factDeleteEvent = new FactDeleteEvent(this, savedFactDto);
        eventPublisher.publishEvent(factDeleteEvent);
    }

    private Fact getFactById(Long id) {
        return factRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fact not found with ID: " + id));
    }
}