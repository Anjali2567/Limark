package ai.leadplus.api.v1.admin.feedbacks;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackReplyRequest {

    @NotBlank(message = "Reply message is required")
    private String message;
}
