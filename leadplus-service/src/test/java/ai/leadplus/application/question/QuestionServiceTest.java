package ai.leadplus.application.question;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.questionsection.QuestionSectionDto;
import ai.leadplus.application.questionsection.QuestionSectionService;
import ai.leadplus.domain.question.Question;
import ai.leadplus.domain.question.QuestionRepository;
import ai.leadplus.domain.question.QuestionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionSearchService questionSearchService;

    @Mock
    private QuestionSectionService questionSectionService;

    @InjectMocks
    private QuestionService questionService;

    private Question question;
    private QuestionDto questionDto;

    @BeforeEach
    void setup() {

        question = Question.builder()
                .id(1L)
                .questionSectionId(1L)
                .type(QuestionType.TEXT)
                .label("Test Question")
                .industryIds(List.of(1L))
                .active(true)
                .build();

        questionDto = QuestionDto.toDto(question);
    }

    @Test
    void createQuestion_ShouldSaveAndReturnDto() {

        when(questionSectionService.findQuestionSectionById(1L))
                .thenReturn(null);

        when(questionRepository.save(any()))
                .thenReturn(question);

        QuestionDto result = questionService.createQuestion(questionDto);

        assertThat(result.getId()).isEqualTo(1L);

        verify(questionRepository).save(any());
        verify(questionSectionService).findQuestionSectionById(1L);
    }

    @Test
    void createQuestion_ShouldThrow_WhenOptionsMissing_ForMultiSelect() {

        questionDto.setType(QuestionType.MULTISELECT);
        questionDto.setOptions(null);

        assertThrows(BadRequestException.class,
                () -> questionService.createQuestion(questionDto));
    }

    @Test
    void createQuestion_ShouldThrow_WhenOptionsProvided_ForTextType() {

        questionDto.setType(QuestionType.TEXT);
        questionDto.setOptions(List.of("A"));

        assertThrows(IllegalArgumentException.class,
                () -> questionService.createQuestion(questionDto));
    }

    @Test
    void updateQuestion_ShouldUpdateAndSave() {

        when(questionRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(question));

        when(questionSectionService.findQuestionSectionById(1L))
                .thenReturn(null);

        when(questionRepository.save(any()))
                .thenReturn(question);

        QuestionDto result = questionService.updateQuestion(String.valueOf(1L), questionDto);

        assertThat(result).isNotNull();
        verify(questionRepository).save(any());
    }

    @Test
    void updateQuestion_ShouldThrow_WhenNotFound() {

        when(questionRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> questionService.updateQuestion(String.valueOf(1L), questionDto));
    }

    @Test
    void deleteQuestion_ShouldDeleteSuccessfully() {

        when(questionRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(question));

        questionService.deleteQuestionById(String.valueOf(1L));

        verify(questionRepository).delete(any(Question.class));
    }

    @Test
    void getAllQuestions_ShouldReturnMappedPage() {

        Page<Question> page = new PageImpl<>(List.of(question));

        when(questionSearchService.searchQuestions(any(), any(), any()))
                .thenReturn(page);

        Page<QuestionDto> result =
                questionService.getAllQuestions(List.of(1L), List.of(1L), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(1L);
    }

    @Test
    void getQuestionById_ShouldReturnDto() {

        when(questionRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(question));

        QuestionDto result = questionService.getQuestionById("1");

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getQuestionById_ShouldThrow_WhenNotFound() {

        when(questionRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> questionService.getQuestionById("1"));
    }

    @Test
    void getQuestionsByIndustryIds_ShouldReturnDtos() {

        when(questionRepository.findByIndustryIdsInAndActiveTrue(any()))
                .thenReturn(List.of(question));

        List<QuestionDto> result =
                questionService.getQuestionsByIndustryIds(List.of(1L));

        assertThat(result).hasSize(1);
    }

    @Test
    void getQuestionsByIds_ShouldReturnDtos() {

        when(questionRepository.findAllById(any()))
                .thenReturn(List.of(question));

        List<QuestionDto> result =
                questionService.getQuestionsByIds(List.of(1L));

        assertThat(result).hasSize(1);
    }

    @Test
    void getQuestionnaireWithSectionByIndustryId_ShouldGroupCorrectly() {

        when(questionRepository.findByIndustryIdsInAndActiveTrue(any()))
                .thenReturn(List.of(question));

        QuestionSectionDto sectionDto = QuestionSectionDto.builder()
                .id(1L)
                .name("Section 1")
                .build();

        when(questionSectionService.getQuestionSectionById(1L))
                .thenReturn(sectionDto);

        List<SectionWithQuestionDto> result =
                questionService.getQuestionnaireWithSectionByIndustryId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getQuestions()).hasSize(1);
    }

    @Test
    void getQuestionnaireWithSectionByIndustryIds_ShouldGroupCorrectly() {

        Page<Question> page = new PageImpl<>(List.of(question));

        when(questionSearchService.searchQuestions(any(), any(), any()))
                .thenReturn(page);

        QuestionSectionDto sectionDto = QuestionSectionDto.builder()
                .id(1L)
                .name("Section 1")
                .build();

        when(questionSectionService.getQuestionSectionsByIds(any()))
                .thenReturn(List.of(sectionDto));

        List<SectionWithQuestionDto> result =
                questionService.getQuestionnaireWithSectionByIndustryIds(List.of(1L));

        assertThat(result).hasSize(1);
    }
}
