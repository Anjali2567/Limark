package ai.leadplus.application.questionsection;

import ai.leadplus.application.exception.DuplicateResourceException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.questionsection.QuestionSection;
import ai.leadplus.domain.questionsection.QuestionSectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionSectionServiceTest {

    @Mock
    private QuestionSectionRepository questionSectionRepository;

    @InjectMocks
    private QuestionSectionService questionSectionService;

    private QuestionSection section;
    private QuestionSectionDto sectionDto;

    @BeforeEach
    void setUp() {
        section = QuestionSection.builder()
                .id(1L)
                .name("Section 1")
                .position(1)
                .active(true)
                .createdAt(LocalDateTime.now())
                .createdBy(1L)
                .updatedAt(LocalDateTime.now())
                .updatedBy(1L)
                .build();

        sectionDto = QuestionSectionDto.builder()
                .id(1L)
                .name("Section 1")
                .position(1)
                .active(true)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .updatedBy(1L)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllQuestionSections_ShouldReturnAllSections() {
        when(questionSectionRepository.findAllByActiveTrue()).thenReturn(List.of(section));

        List<QuestionSectionDto> result = questionSectionService.getAllQuestionSections();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        verify(questionSectionRepository).findAllByActiveTrue();
    }

    @Test
    void getQuestionSectionById_ShouldReturnSection() {
        when(questionSectionRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(section));

        QuestionSectionDto result = questionSectionService.getQuestionSectionById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        verify(questionSectionRepository).findByIdAndActiveTrue(1L);
    }

    @Test
    void getQuestionSectionById_ShouldThrow_WhenNotFound() {
        when(questionSectionRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionSectionService.getQuestionSectionById(2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Question section not found with id: 2");
    }

    @Test
    void createQuestionSection_ShouldSaveAndReturn() {
        when(questionSectionRepository.existsByNameIgnoreCaseAndActiveTrue("Section 1")).thenReturn(false);
        when(questionSectionRepository.save(any(QuestionSection.class))).thenReturn(section);

        QuestionSectionDto result = questionSectionService.createQuestionSection(sectionDto);

        assertThat(result.getId()).isEqualTo(1L);
        verify(questionSectionRepository).save(any(QuestionSection.class));
    }

    @Test
    void createQuestionSection_ShouldThrow_WhenDuplicate() {
        when(questionSectionRepository.existsByNameIgnoreCaseAndActiveTrue("Section 1")).thenReturn(true);

        assertThatThrownBy(() -> questionSectionService.createQuestionSection(sectionDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Question section with name 'Section 1' already exists.");
    }

    @Test
    void updateQuestionSection_ShouldUpdateAndReturn() {
        QuestionSection updatedSection = QuestionSection.builder()
                .id(1L)
                .name("Updated Section")
                .position(2)
                .active(true)
                .build();

        QuestionSectionDto updateDto = QuestionSectionDto.builder()
                .name("Updated Section")
                .position(2)
                .build();

        when(questionSectionRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(section));
        when(questionSectionRepository.existsByNameIgnoreCaseAndActiveTrue("Updated Section")).thenReturn(false);
        when(questionSectionRepository.save(any(QuestionSection.class))).thenReturn(updatedSection);

        QuestionSectionDto result = questionSectionService.updateQuestionSection(1L, updateDto);

        assertThat(result.getName()).isEqualTo("Updated Section");
        assertThat(result.getPosition()).isEqualTo(2);
        verify(questionSectionRepository).save(any(QuestionSection.class));
    }

    @Test
    void updateQuestionSection_ShouldThrow_WhenDuplicateName() {
        QuestionSectionDto updateDto = QuestionSectionDto.builder()
                .name("Duplicate Name")
                .build();

        when(questionSectionRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(section));
        when(questionSectionRepository.existsByNameIgnoreCaseAndActiveTrue("Duplicate Name")).thenReturn(true);

        assertThatThrownBy(() -> questionSectionService.updateQuestionSection(1L, updateDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Question section with name 'Duplicate Name' already exists.");
    }

    @Test
    void getQuestionSectionsByIds_ShouldReturnSections() {
        when(questionSectionRepository.findAllByIdInAndActiveTrue(Set.of(1L))).thenReturn(List.of(section));

        List<QuestionSectionDto> result = questionSectionService.getQuestionSectionsByIds(Set.of(1L));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        verify(questionSectionRepository).findAllByIdInAndActiveTrue(Set.of(1L));
    }

    @Test
    void bulkSaveQuestionSections_ShouldCallCreateAndUpdate() {
        QuestionSectionDto newSection = QuestionSectionDto.builder()
                .name("New Section")
                .build();

        QuestionSectionDto existingSection = QuestionSectionDto.builder()
                .id(1L)
                .name("Updated Section")
                .position(2)
                .build();

        when(questionSectionRepository.findAllByIdInAndActiveTrue(Set.of(1L))).thenReturn(List.of(section));
        when(questionSectionRepository.existsByNameIgnoreCaseAndActiveTrue("Updated Section")).thenReturn(false);
        when(questionSectionRepository.existsByNameIgnoreCaseAndActiveTrue("New Section")).thenReturn(false);
        when(questionSectionRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<QuestionSectionDto> result = questionSectionService.bulkSaveQuestionSections(List.of(newSection, existingSection));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("New Section");
        assertThat(result.get(1).getName()).isEqualTo("Updated Section");

        verify(questionSectionRepository).saveAll(anyList());
    }
}
