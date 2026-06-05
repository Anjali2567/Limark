package ai.leadplus.api.v1.admin.feedbacks;

import ai.leadplus.api.v1.feedbacks.FeedbackResponse;
import ai.leadplus.application.feedbacks.FeedbackDto;
import ai.leadplus.application.feedbacks.FeedbackService;
import ai.leadplus.domain.feedbacks.FeedbackStatus;
import ai.leadplus.domain.feedbacks.FeedbackType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/admin/feedbacks")
@RequiredArgsConstructor
@Tag(name = "Admin Feedbacks", description = "Application Admin Feedback APIs")
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "Get Feedback by ID")
    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> getFeedbackById(
            @PathVariable Long id) {

        log.info("[GET] Get Feedback id={}", id);
        FeedbackDto dto = feedbackService.getById(id);
        FeedbackResponse response = FeedbackResponse.fromDto(dto);
        log.info("[GET] Returning Feedback with ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search Feedbacks")
    @GetMapping
    public ResponseEntity<Page<FeedbackResponse>> searchFeedbacks(
            @RequestParam(required = false) String workspaceId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<FeedbackStatus> status,
            @RequestParam(required = false) List<FeedbackType> types,
            Pageable pageable) {

        log.info("[GET] Search Feedbacks query={}", query);
        Page<FeedbackDto> page = feedbackService.searchFeedbacks(workspaceId, userId, status, types, query, pageable);
        Page<FeedbackResponse> response = page.map(FeedbackResponse::fromDto);
        log.info("[GET] Returning Search {} Feedbacks ", response.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Feedback Status to REVIEWED")
    @PutMapping("/{id}/reviewed")
    public ResponseEntity<FeedbackResponse> updateStatus(
            @PathVariable Long id) {

        log.info("[PUT] Received request to Update Feedback status id={} to {}", id, FeedbackStatus.REVIEWED);
        FeedbackDto dto = feedbackService.updateStatus(id, FeedbackStatus.REVIEWED);
        FeedbackResponse response = FeedbackResponse.fromDto(dto);
        log.info("[PUT] Updated Feedback status with Id: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Send Reply to Feedback")
    @PostMapping("/{id}/reply")
    public ResponseEntity<FeedbackResponse> sendReply(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackReplyRequest request) {

        log.info("[POST] Received request to reply for Feedback id={}", id);
        FeedbackDto dto = feedbackService.sendReplyFeedback(id, request.getMessage());
        log.info("[POST] Send reply for Feedback for id={}", id);
        return ResponseEntity.ok(FeedbackResponse.fromDto(dto));
    }
}
