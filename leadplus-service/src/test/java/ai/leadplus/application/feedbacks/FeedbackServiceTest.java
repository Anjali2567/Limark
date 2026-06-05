package ai.leadplus.application.feedbacks;

import ai.leadplus.application.aws.EmailService;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.domain.feedbacks.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeedbackServiceTest {

    @InjectMocks
    private FeedbackService feedbackService;

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private FeedbackSearchService feedbackSearchService;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    private FeedbackDto feedbackDto;
    private UserDto userDto;
    private Feedback feedback;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        feedbackDto = FeedbackDto.builder()
                .type(FeedbackType.GENERAL)
                .message("Test feedback")
                .rating(4)
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .company("TestCompany")
                .email("john@example.com")
                .build();

        feedback = feedbackDto.toEntity();
        feedback.setId(1L);
        feedback.setUserId(userDto.getId());
        feedback.setUsername(userDto.getName());
        feedback.setCompanyName(userDto.getCompany());
        feedback.setTenantId(1L);
        feedback.setWorkspaceId(2L);
        feedback.setStatus(FeedbackStatus.NEW);
        feedback.setFeedbackDate(LocalDateTime.now());
    }

    @Test
    void testCreateFeedback_success() {
        when(userService.getUserById(1L)).thenReturn(userDto);
        when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);

        FeedbackDto result = feedbackService.createFeedback("1", "2", "1", feedbackDto);

        assertNotNull(result);
        assertEquals(feedback.getMessage(), result.getMessage());
        assertEquals(feedback.getStatus(), result.getStatus());
        verify(feedbackRepository, times(1)).save(any(Feedback.class));
    }

    @Test
    void testCreateFeedback_invalidRating() {
        feedbackDto.setRating(6);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                feedbackService.createFeedback("tenant1", "workspace1", "user1", feedbackDto)
        );

        assertEquals("Rating must be between 1 and 5", exception.getMessage());
    }

    @Test
    void testGetById_success() {
        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));

        FeedbackDto result = feedbackService.getById(1L);

        assertNotNull(result);
        assertEquals(feedback.getId(), result.getId());
    }

    @Test
    void testGetById_notFound() {
        when(feedbackRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                feedbackService.getById(1L)
        );

        assertEquals("Feedback not found with id: 1", exception.getMessage());
    }

    @Test
    void testSearchFeedbacks() {
        Pageable pageable = Pageable.unpaged();
        Page<Feedback> page = new PageImpl<>(List.of(feedback));

        when(feedbackSearchService.searchFeedbacks("workspace1", "user1", null, null, null, pageable))
                .thenReturn(page);

        Page<FeedbackDto> result = feedbackService.searchFeedbacks("workspace1", "user1", null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(feedback.getMessage(), result.getContent().getFirst().getMessage());
    }

    @Test
    void testUpdateStatus_success() {
        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));
        when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);

        FeedbackDto result = feedbackService.updateStatus(1L, FeedbackStatus.REVIEWED);

        assertEquals(FeedbackStatus.REVIEWED, result.getStatus());
    }

    @Test
    void testUpdateStatus_invalid() {
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                feedbackService.updateStatus(1L, FeedbackStatus.NEW)
        );

        assertEquals("Invalid status", exception.getMessage());
    }

    @Test
    void testSendReplyFeedback_success() {
        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));
        when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);
        when(userService.getUserById(1L)).thenReturn(userDto);

        FeedbackDto result = feedbackService.sendReplyFeedback(1L, "Thanks for your feedback!");

        assertEquals(FeedbackStatus.RESPONDED, result.getStatus());
        verify(emailService, times(1))
                .sendFeedbackReceivedEmail(userDto.getEmail(), userDto.getName(), "Thanks for your feedback!");
    }
}
