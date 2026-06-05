package ai.leadplus.api.v1.admin.question;

import ai.leadplus.application.question.QuestionDto;
import ai.leadplus.application.question.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/admin/questions")
@RequiredArgsConstructor
@Tag(name = "Admin Question", description = "Admin Question APIs")
public class AdminQuestionController {

    private final QuestionService questionService;

    @Operation(summary = "Get All Questions")
    @GetMapping
    public ResponseEntity<Page<AdminQuestionResponse>> getAllQuestion(
            @RequestParam(required = false) List<Long> questionIds,
            @RequestParam(required = false) List<Long> industryIds,
            Pageable pageable) {
        log.info("[GET] Received request to get all Questions");
        Page<AdminQuestionResponse> adminQuestionResponses = questionService
                .getAllQuestions(questionIds, industryIds, pageable)
                .map(AdminQuestionResponse::toResponse);
        log.info("[GET] Returning {} Questions (current page size: {})",
                adminQuestionResponses.getTotalElements(),
                adminQuestionResponses.getNumberOfElements());
        return ResponseEntity.ok(adminQuestionResponses);
    }

    @Operation(summary = "Get All Questions")
    @GetMapping("/get-all")
    public ResponseEntity<List<AdminQuestionResponse>> getAllQuestions(@RequestParam(required = false) List<Long> industryIds) {
        log.info("[GET] Received request to get all Questions");
        List<AdminQuestionResponse> adminQuestionResponses = questionService
                .getQuestionsByIndustryIds(industryIds).stream().map(AdminQuestionResponse::toResponse).toList();
        log.info("[GET] Returning Number Questions {}", adminQuestionResponses.size());
        return ResponseEntity.ok(adminQuestionResponses);
    }

    @Operation(summary = "Add New Question")
    @PostMapping
    public ResponseEntity<AdminQuestionResponse> addQuestion(
            @Valid @RequestBody AdminQuestionRequest adminQuestionRequest) {
        log.info("[POST] Received request to Create Question");
        QuestionDto questionDto = questionService.createQuestion(adminQuestionRequest.toDto());
        AdminQuestionResponse questionResponse = AdminQuestionResponse.toResponse(questionDto);
        log.info("[POST] Created Question with Id: {}", questionResponse.getId());
        return new ResponseEntity<>(questionResponse, HttpStatus.CREATED);
    }

    @Operation(summary = "Update Question by ID")
    @PutMapping("/{id}")
    public ResponseEntity<AdminQuestionResponse> updateQuestionById(@PathVariable String id,
                                                                    @Valid @RequestBody AdminQuestionRequest adminQuestionRequest) {
        log.info("[PUT] Received request to update Question By Id:{}",id);
        QuestionDto questionDto = questionService.updateQuestion(id, adminQuestionRequest.toDto());
        log.info("[PUT] Updated Question by ID {}", id);
        return ResponseEntity.ok(AdminQuestionResponse.toResponse(questionDto));
    }

    @Operation(summary = "Delete Question by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestionById(@PathVariable String id) {
        log.info("[DELETE] Received request to delete Question By Id:{}",id);
        questionService.deleteQuestionById(id);
        log.info("[DELETE] Question with ID {} has been deleted.", id);
        return ResponseEntity.noContent().build();
    }
}