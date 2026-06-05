package ai.leadplus.api.v1.admin.questionsection;

import ai.leadplus.api.v1.admin.question.AdminQuestionResponse;
import ai.leadplus.api.v1.admin.question.AdminQuestionUpdateRequest;
import ai.leadplus.application.question.QuestionDto;
import ai.leadplus.application.question.QuestionService;
import ai.leadplus.application.questionsection.QuestionSectionDto;
import ai.leadplus.application.questionsection.QuestionSectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/admin/question-sections")
@RequiredArgsConstructor
@Tag(name = "Admin Question Section APIs", description = "APIs for managing question sections")
public class AdminQuestionSectionController {

    private final QuestionSectionService questionSectionService;
    private final QuestionService questionService;

    @Operation(summary = "Get All Question Sections")
    @GetMapping
    public ResponseEntity<List<AdminQuestionSectionResponse>> getAllQuestionSections() {
        log.info("[GET] Received request to get all Question Sections");
        List<QuestionSectionDto> questionSectionDtoList = questionSectionService.getAllQuestionSections();
        List<AdminQuestionSectionResponse> questionSectionResponses = questionSectionDtoList.stream()
                .map(AdminQuestionSectionResponse::toResponse)
                .toList();
        log.info("[GET] Returning {} Question Sections", questionSectionResponses.size());
        return ResponseEntity.ok(questionSectionResponses);
    }

    @Operation(summary = "Get Question Section by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AdminQuestionSectionResponse> getQuestionSectionById(@PathVariable Long id) {
        log.info("[GET] Received request to get Question Section by ID: {}", id);
        QuestionSectionDto questionSectionDto = questionSectionService.getQuestionSectionById(id);
        AdminQuestionSectionResponse response = AdminQuestionSectionResponse.toResponse(questionSectionDto);
        log.info("[GET] Retrieved Question Section by ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create New Question Section")
    @PostMapping
    public ResponseEntity<AdminQuestionSectionResponse> createQuestionSection(
            @Valid @RequestBody AdminQuestionSectionRequest questionSectionRequest) {
        log.info("[POST] Received request to create a new Question Section");
        QuestionSectionDto questionSectionDto = questionSectionService.createQuestionSection(questionSectionRequest.toDto());
        AdminQuestionSectionResponse response = AdminQuestionSectionResponse.toResponse(questionSectionDto);
        log.info("[POST] Created new Question Section with ID: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Update Question Section by ID")
    @PutMapping("/{id}")
    public ResponseEntity<AdminQuestionSectionResponse> updateQuestionSectionById(
            @PathVariable Long id,
            @Valid @RequestBody AdminQuestionSectionRequest questionSectionRequest) {
        log.info("[PUT] Received request to update Question Section by ID: {}", id);
        QuestionSectionDto questionSectionDto = questionSectionService.updateQuestionSection(id, questionSectionRequest.toDto());
        AdminQuestionSectionResponse response = AdminQuestionSectionResponse.toResponse(questionSectionDto);
        log.info("[PUT] Updated Question Section with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Bulk Save Questions for a Section")
    @PutMapping("/{sectionId}/questions")
    public ResponseEntity<List<AdminQuestionResponse>> bulkSaveQuestions(
            @PathVariable Long sectionId,
            @Valid @RequestBody List<AdminQuestionUpdateRequest> questionRequests) {

        log.info("[PUT] Received request to bulk save {} questions for section ID: {}",
                questionRequests.size(), sectionId);

        List<QuestionDto> dtos = questionRequests.stream()
                .map(AdminQuestionUpdateRequest::toDto)
                .toList();

        List<AdminQuestionResponse> responses = questionService
                .bulkSaveQuestions(sectionId, dtos)
                .stream()
                .map(AdminQuestionResponse::toResponse)
                .toList();

        log.info("[PUT] Bulk saved {} questions for section ID: {}", responses.size(), sectionId);

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Bulk Save Sections")
    @PutMapping
    public ResponseEntity<List<AdminQuestionSectionResponse>> bulkSaveSections(
            @Valid @RequestBody List<AdminQuestionSectionUpdateRequest> sectionRequests) {
        log.info("[PUT] Received request to bulk save {} sections", sectionRequests.size());
        List<QuestionSectionDto> questionSectionDtos = sectionRequests.stream()
                .map(AdminQuestionSectionRequest::toDto)
                .toList();
        questionSectionDtos = questionSectionService.bulkSaveQuestionSections(questionSectionDtos);
        List<AdminQuestionSectionResponse> responses = questionSectionDtos.stream()
                .map(AdminQuestionSectionResponse::toResponse).toList();
        log.info("[PUT] Bulk saved {} sections", questionSectionDtos.size());
        return ResponseEntity.ok(responses);
    }
}
