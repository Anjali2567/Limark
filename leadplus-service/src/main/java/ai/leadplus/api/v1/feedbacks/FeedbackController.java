package ai.leadplus.api.v1.feedbacks;

import ai.leadplus.api.common.UserValidator;
import ai.leadplus.application.feedbacks.FeedbackDto;
import ai.leadplus.application.feedbacks.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/tenants/{tenantId}/workspaces/{workspaceId}/feedbacks")
@RequiredArgsConstructor
@Tag(name = "Feedbacks", description = "Application Feedback APIs")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserValidator userValidator;

    @Operation(summary = "Create Feedback")
    @PostMapping
    public ResponseEntity<FeedbackResponse> createFeedback(
            @PathVariable Long tenantId,
            @PathVariable String workspaceId,
            @Valid @RequestBody FeedbackRequest request) {

        log.info("[POST] Create Feedback for tenantId={}, workspaceId={}", tenantId, workspaceId);
        String userId = userValidator.getAuthenticatedUserId();
        FeedbackDto dto = request.toDto();
        FeedbackDto saved = feedbackService.createFeedback(String.valueOf(tenantId), workspaceId, userId, dto);
        FeedbackResponse response = FeedbackResponse.fromDto(saved);
        log.info("[POST] Feedback created with id={}", saved.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}
