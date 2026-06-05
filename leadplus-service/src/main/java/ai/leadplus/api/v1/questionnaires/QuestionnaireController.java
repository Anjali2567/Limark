package ai.leadplus.api.v1.questionnaires;

import ai.leadplus.application.question.QuestionService;
import ai.leadplus.application.question.SectionWithQuestionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/industries/questionnaires")
@RequiredArgsConstructor
@Tag(name = "Questionnaire", description = "Questionnaire APIs")
public class QuestionnaireController {

    private final QuestionService questionService;

    @Operation(summary = "Get Questionnaire by IndustryId")
    @GetMapping("/{industryId}")
    public ResponseEntity<List<QuestionnaireResponse>> getQuestionnaireById(@PathVariable Long industryId) {
        log.info("[GET] Received request to get Questionnaire by Industry Id: {}", industryId);
        List<SectionWithQuestionDto> sectionWithQuestionDtos = questionService.getQuestionnaireWithSectionByIndustryId(industryId);
        List<QuestionnaireResponse> questionnaireResponse = sectionWithQuestionDtos.stream()
                .map(QuestionnaireResponse::toResponse)
                .toList();
        log.info("[GET] Retrieved {} Questionnaire(s) by Industry Id: {}",
                questionnaireResponse.size(),
                industryId);
        return ResponseEntity.ok(questionnaireResponse);
    }

    @Operation(summary = "Get Questionnaire By IndustryIds")
    @GetMapping
    public ResponseEntity<List<QuestionnaireResponse>> getQuestionnaireByIndustryId(@RequestParam List<Long> industryIds) {
        log.info("[GET] Received request to get Questionnaire by Industry Ids : {}", industryIds.size());
        List<SectionWithQuestionDto> sectionWithQuestionDtos = questionService.getQuestionnaireWithSectionByIndustryIds(industryIds);
        List<QuestionnaireResponse> questionnaireResponse = sectionWithQuestionDtos.stream()
                .map(QuestionnaireResponse::toResponse)
                .toList();
        log.info("[GET] Retrieved {} Questions by Industry Ids: {}", questionnaireResponse.size(), industryIds.size());
        return ResponseEntity.ok(questionnaireResponse);
    }
}
