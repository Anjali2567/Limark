package ai.leadplus.application.feedbacks;

import ai.leadplus.domain.feedbacks.Feedback;
import ai.leadplus.domain.feedbacks.FeedbackRepository;
import ai.leadplus.domain.feedbacks.FeedbackStatus;
import ai.leadplus.domain.feedbacks.FeedbackType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackSearchServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FeedbackSearchService feedbackSearchService;

    private Feedback feedback;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        feedback = Feedback.builder()
                .id(1L)
                .username("testuser")
                .companyName("Test Company")
                .message("Test feedback message")
                .status(FeedbackStatus.NEW)
                .type(FeedbackType.BUG_REPORT)
                .build();
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void searchFeedbacks_noFilters_returnsAllFeedbacks() {
        when(feedbackRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(Collections.singletonList(feedback), pageable, 1));

        Page<Feedback> result = feedbackSearchService.searchFeedbacks(null, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(feedbackRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchFeedbacks_withQuery_filtersByQuery() {
        when(feedbackRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(Collections.singletonList(feedback), pageable, 1));

        Page<Feedback> result = feedbackSearchService.searchFeedbacks(null, null, null, null, "test", pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(feedbackRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchFeedbacks_withStatus_filtersByStatus() {
        List<FeedbackStatus> statuses = Collections.singletonList(FeedbackStatus.NEW);
        when(feedbackRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(Collections.singletonList(feedback), pageable, 1));

        Page<Feedback> result = feedbackSearchService.searchFeedbacks(null, null, statuses, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(feedbackRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchFeedbacks_withType_filtersByType() {
        List<FeedbackType> types = Collections.singletonList(FeedbackType.BUG_REPORT);
        when(feedbackRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(Collections.singletonList(feedback), pageable, 1));

        Page<Feedback> result = feedbackSearchService.searchFeedbacks(null, null, null, types, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(feedbackRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchFeedbacks_emptyResult_returnsEmptyPage() {
        when(feedbackRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

        Page<Feedback> result = feedbackSearchService.searchFeedbacks(null, null, null, null, "nonexistent", pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(feedbackRepository).findAll(any(Specification.class), eq(pageable));
    }
}
