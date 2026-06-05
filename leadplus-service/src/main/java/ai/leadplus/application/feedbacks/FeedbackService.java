package ai.leadplus.application.feedbacks;

import ai.leadplus.application.aws.EmailService;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.domain.feedbacks.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackSearchService feedbackSearchService;
    private final UserService userService;
    private final EmailService emailService;

    public FeedbackDto createFeedback(String tenantId, String workspaceId, String userId, FeedbackDto dto) {

        validateRating(dto.getRating());
        UserDto user = userService.getUserById(Long.parseLong(userId));

        Feedback feedback = dto.toEntity();
        feedback.setTenantId(Long.parseLong(tenantId));
        feedback.setWorkspaceId(Long.parseLong(workspaceId));
        feedback.setUserId(Long.parseLong(userId));
        feedback.setUsername(user.getName());
        feedback.setCompanyName(user.getCompany());
        feedback.setStatus(FeedbackStatus.NEW);
        feedback.setFeedbackDate(LocalDateTime.now());

        return FeedbackDto.fromEntity(feedbackRepository.save(feedback));
    }

    public FeedbackDto getById(Long id) {
        Feedback feedback = findById(id);

        return FeedbackDto.fromEntity(feedback);
    }

    public Page<FeedbackDto> searchFeedbacks(String workspaceId,
                                             String userId,
                                             List<FeedbackStatus> statuses,
                                             List<FeedbackType> types,
                                             String query,
                                             Pageable pageable) {
        return feedbackSearchService
                .searchFeedbacks(workspaceId, userId, statuses, types, query, pageable)
                .map(FeedbackDto::fromEntity);
    }

    public FeedbackDto updateStatus(Long id, FeedbackStatus status) {
        if(!FeedbackStatus.REVIEWED.equals(status)) {
            throw new BadRequestException("Invalid status");
        }

        Feedback feedback = findById(id);
        feedback.setStatus(status);

        return FeedbackDto.fromEntity(feedbackRepository.save(feedback));
    }

    public FeedbackDto sendReplyFeedback(Long id, String message) {
        Feedback feedback = findById(id);
        UserDto user = userService.getUserById(feedback.getUserId());
        emailService.sendFeedbackReceivedEmail(user.getEmail(), user.getName(), message);

        feedback.setStatus(FeedbackStatus.RESPONDED);
        feedback = feedbackRepository.save(feedback);

        return FeedbackDto.fromEntity(feedback);
    }

    private Feedback findById(Long id){
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id: " + id));
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }
    }
}