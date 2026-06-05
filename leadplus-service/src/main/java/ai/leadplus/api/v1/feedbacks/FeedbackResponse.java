package ai.leadplus.api.v1.feedbacks;

import ai.leadplus.application.feedbacks.FeedbackDto;
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
public class FeedbackResponse {

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

    public static FeedbackResponse fromDto(FeedbackDto dto) {
        return FeedbackResponse.builder()
                .id(dto.getId())
                .tenantId(dto.getTenantId())
                .workspaceId(dto.getWorkspaceId())
                .userId(dto.getUserId())
                .username(dto.getUsername())
                .companyName(dto.getCompanyName())
                .type(dto.getType())
                .message(dto.getMessage())
                .rating(dto.getRating())
                .status(dto.getStatus())
                .feedbackDate(dto.getFeedbackDate())
                .createdBy(dto.getCreatedBy())
                .createdAt(dto.getCreatedAt())
                .updatedBy(dto.getUpdatedBy())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
