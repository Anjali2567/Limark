package ai.leadplus.application.facts;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.facts.Fact;
import ai.leadplus.domain.facts.FactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FactServiceTest {

    @Mock
    private FactRepository factRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FactService factService;

    private Fact fact;
    private FactDto factDto;

    @BeforeEach
    void setUp() {
        fact = Fact.builder()
                .id(1L)
                .fact("Did you know?")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        factDto = FactDto.builder()
                .id(1L)
                .fact("Did you know?")
                .createdAt(fact.getCreatedAt())
                .updatedAt(fact.getUpdatedAt())
                .build();
    }

    @Test
    void addFact_shouldSaveFactAndPublishEvent() {
        when(factRepository.save(any(Fact.class))).thenAnswer(i -> i.getArgument(0));

        FactDto result = factService.addFact(factDto);

        assertThat(result.getFact()).isEqualTo("Did you know?");
        verify(factRepository).save(any(Fact.class));

        ArgumentCaptor<FactCreatedEvent> eventCaptor = ArgumentCaptor.forClass(FactCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getFactDto().getFact()).isEqualTo("Did you know?");
    }

    @Test
    void updateFact_shouldUpdateFactAndPublishEvent() {
        FactDto updateDto = FactDto.builder().fact("New fact").build();
        when(factRepository.findById(1L)).thenReturn(Optional.of(fact));
        when(factRepository.save(any(Fact.class))).thenAnswer(i -> i.getArgument(0));

        FactDto result = factService.updateFact(1L, updateDto);

        assertThat(result.getFact()).isEqualTo("New fact");
        verify(factRepository).save(fact);

        ArgumentCaptor<FactUpdateEvent> eventCaptor = ArgumentCaptor.forClass(FactUpdateEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getFactDto().getFact()).isEqualTo("New fact");
    }

    @Test
    void updateFact_shouldThrow_whenFactNotFound() {
        when(factRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> factService.updateFact(2L, factDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Fact not found with ID: 2");

        verify(factRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void deleteFact_shouldDeleteFactAndPublishEvent() {
        when(factRepository.findById(1L)).thenReturn(Optional.of(fact));

        factService.deleteFact(1L);

        verify(factRepository).delete(fact);

        ArgumentCaptor<FactDeleteEvent> eventCaptor = ArgumentCaptor.forClass(FactDeleteEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getFactDto().getId()).isEqualTo(1L);
    }

    @Test
    void deleteFact_shouldThrow_whenFactNotFound() {
        when(factRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> factService.deleteFact(2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Fact not found with ID: 2");

        verify(factRepository, never()).delete(any());
        verifyNoInteractions(eventPublisher);
    }
}
