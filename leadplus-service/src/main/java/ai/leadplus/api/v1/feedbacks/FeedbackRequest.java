package ai.leadplus.api.v1.feedbacks;

import ai.leadplus.application.feedbacks.FeedbackDto;
import ai.leadplus.domain.feedbacks.FeedbackType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequest {

    @NotNull(message = "Feedback type is required")
    private FeedbackType type;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    public FeedbackDto toDto() {
        return FeedbackDto.builder()
                .type(type)
                .message(message)
                .rating(rating)
                .build();
    }
}