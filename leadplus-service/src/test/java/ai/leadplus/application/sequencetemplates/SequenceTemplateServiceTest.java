package ai.leadplus.application.sequencetemplates;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.sequencetemplates.SequenceTemplate;
import ai.leadplus.domain.sequencetemplates.SequenceTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SequenceTemplateServiceTest {

    @Mock
    private SequenceTemplateRepository sequenceTemplateRepository;

    @InjectMocks
    private SequenceTemplateService sequenceTemplateService;

    @Test
    void createSequenceTemplate_shouldReturnDto_whenValidInput() {
        SequenceTemplateDto inputDto = SequenceTemplateDto.builder()
                .id(1L)
                .name("Template Name")
                .stepCount(3)
                .defaultDelays(List.of(3, 2))
                .purpose("Purpose")
                .build();
        SequenceTemplate sequenceTemplate = inputDto.toEntity();
        when(sequenceTemplateRepository.save(any(SequenceTemplate.class))).thenReturn(sequenceTemplate);

        SequenceTemplateDto result = sequenceTemplateService.createSequenceTemplate(inputDto);

        assertEquals(1L, result.getId());
        assertEquals("Template Name", result.getName());
        verify(sequenceTemplateRepository).save(any(SequenceTemplate.class));
    }

    @Test
    void getSequenceTemplateById_shouldReturnDto_whenTemplateExists() {
        SequenceTemplate sequenceTemplate = SequenceTemplate.builder()
                .id(1L)
                .name("Template Name")
                .stepCount(3)
                .defaultDelays(List.of(3, 2))
                .purpose("Purpose")
                .build();
        when(sequenceTemplateRepository.findById(1L)).thenReturn(Optional.of(sequenceTemplate));

        SequenceTemplateDto result = sequenceTemplateService.getSequenceTemplateById("1");

        assertEquals(1L, result.getId());
        assertEquals("Template Name", result.getName());
        verify(sequenceTemplateRepository).findById(1L);
    }

    @Test
    void getSequenceTemplateById_shouldThrowResourceNotFoundException_whenTemplateDoesNotExist() {
        when(sequenceTemplateRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sequenceTemplateService.getSequenceTemplateById("1"));
        verify(sequenceTemplateRepository).findById(1L);
    }

    @Test
    void updateSequenceTemplateById_shouldReturnUpdatedDto_whenTemplateExists() {
        SequenceTemplate existingTemplate = SequenceTemplate.builder()
                .id(1L)
                .name("Old Name")
                .stepCount(3)
                .defaultDelays(List.of(3, 2))
                .purpose("Old Purpose")
                .build();
        SequenceTemplateDto updateDto = SequenceTemplateDto.builder()
                .id(1L)
                .name("New Name")
                .stepCount(5)
                .defaultDelays(List.of(10))
                .purpose("New Purpose")
                .build();
        when(sequenceTemplateRepository.findById(1L)).thenReturn(Optional.of(existingTemplate));
        when(sequenceTemplateRepository.save(any(SequenceTemplate.class))).thenReturn(existingTemplate);

        SequenceTemplateDto result = sequenceTemplateService.updateSequenceTemplateById("1", updateDto);

        assertEquals(1L, result.getId());
        assertEquals("New Name", result.getName());
        verify(sequenceTemplateRepository).findById(1L);
        verify(sequenceTemplateRepository).save(any(SequenceTemplate.class));
    }

    @Test
    void updateSequenceTemplateById_shouldThrowResourceNotFoundException_whenTemplateDoesNotExist() {
        SequenceTemplateDto updateDto = SequenceTemplateDto.builder()
                .id(1L)
                .name("New Name")
                .stepCount(5)
                .defaultDelays(List.of(10))
                .purpose("New Purpose")
                .build();
        when(sequenceTemplateRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sequenceTemplateService.updateSequenceTemplateById("1", updateDto));
        verify(sequenceTemplateRepository).findById(1L);
    }

    @Test
    void deleteSequenceTemplateById_shouldDeleteTemplate_whenTemplateExists() {
        SequenceTemplate sequenceTemplate = SequenceTemplate.builder()
                .id(1L)
                .name("Template Name")
                .stepCount(3)
                .defaultDelays(List.of(3, 2))
                .purpose("Purpose")
                .build();
        when(sequenceTemplateRepository.findById(1L)).thenReturn(Optional.of(sequenceTemplate));

        sequenceTemplateService.deleteSequenceTemplateById("1");

        verify(sequenceTemplateRepository).findById(1L);
        verify(sequenceTemplateRepository).delete(sequenceTemplate);
    }

    @Test
    void deleteSequenceTemplateById_shouldThrowResourceNotFoundException_whenTemplateDoesNotExist() {
        when(sequenceTemplateRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sequenceTemplateService.deleteSequenceTemplateById("1"));
        verify(sequenceTemplateRepository).findById(1L);
    }
}
