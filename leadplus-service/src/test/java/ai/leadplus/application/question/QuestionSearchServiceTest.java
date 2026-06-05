package ai.leadplus.application.question;

import ai.leadplus.domain.question.Question;
import ai.leadplus.domain.question.QuestionRepository;
import ai.leadplus.domain.question.QuestionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionSearchServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private QuestionSearchService questionSearchService;

    private Question question1;
    private Question question2;

    private Pageable pageable;

    @BeforeEach
    void setup() {
        question1 = Question.builder()
                .id(1L)
                .questionSectionId(1L)
                .type(QuestionType.TEXT)
                .label("Question 1")
                .industryIds(List.of(1L))
                .active(true)
                .build();

        question2 = Question.builder()
                .id(2L)
                .questionSectionId(1L)
                .type(QuestionType.RADIO)
                .label("Question 2")
                .industryIds(List.of(1L, 2L))
                .active(true)
                .build();

        pageable = PageRequest.of(0, 10, Sort.by("id"));
    }

    @Test
    void searchQuestions_ShouldReturnAll_WhenNoFilters() {

        when(questionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(question1, question2), pageable, 2));

        Page<Question> result = questionSearchService.searchQuestions(null, null, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(questionRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchQuestions_ShouldFilterByQuestionIds() {

        List<Long> questionIds = List.of(1L);

        when(questionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(question1), pageable, 1));

        Page<Question> result = questionSearchService.searchQuestions(questionIds, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);

        verify(questionRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchQuestions_ShouldReturnEmpty_WhenNoMatch() {

        when(questionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<Question> result = questionSearchService.searchQuestions(List.of(1L), List.of(1L), pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);

        verify(questionRepository).findAll(any(Specification.class), eq(pageable));
    }
}
