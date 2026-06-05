package ai.leadplus.application.feedbacks;

import ai.leadplus.domain.feedbacks.Feedback;
import ai.leadplus.domain.feedbacks.FeedbackStatus;
import ai.leadplus.domain.feedbacks.FeedbackType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDto {

    private Long id;
    private Long tenantId;
    private Long workspaceId;
    private Long userId;
    private String username;
    private String companyName;
    private FeedbackType type;
    private String message;
    private Integer rating;
    private FeedbackStatus status;
    private LocalDateTime feedbackDate;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static FeedbackDto fromEntity(Feedback feedback) {
        if (feedback == null) return null;

        return FeedbackDto.builder()
                .id(feedback.getId())
                .tenantId(feedback.getTenantId())
                .workspaceId(feedback.getWorkspaceId())
                .userId(feedback.getUserId())
                .username(feedback.getUsername())
                .companyName(feedback.getCompanyName())
                .type(feedback.getType())
                .message(feedback.getMessage())
                .rating(feedback.getRating())
                .status(feedback.getStatus())
                .feedbackDate(feedback.getFeedbackDate())
                .createdBy(feedback.getCreatedBy())
                .createdAt(feedback.getCreatedAt())
                .updatedBy(feedback.getUpdatedBy())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }

    public Feedback toEntity() {
        return Feedback.builder()
                .id(id)
                .tenantId(tenantId)
                .workspaceId(workspaceId)
                .userId(userId)
                .username(username)
                .companyName(companyName)
                .type(type)
                .message(message)
                .rating(rating)
                .status(status)
                .feedbackDate(feedbackDate)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
