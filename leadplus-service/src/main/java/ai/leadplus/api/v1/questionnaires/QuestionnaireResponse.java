package ai.leadplus.api.v1.questionnaires;

import ai.leadplus.application.question.SectionWithQuestionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireResponse {
    private String sectionId;
    private String sectionName;
    private Integer position;
    private List<QuestionResponse> questions;

    public static QuestionnaireResponse toResponse(SectionWithQuestionDto sectionWithQuestionDto) {
        return QuestionnaireResponse.builder()
                .sectionId(sectionWithQuestionDto.getSectionId())
                .sectionName(sectionWithQuestionDto.getSectionName())
                .position(sectionWithQuestionDto.getPosition())
                .questions(
                        sectionWithQuestionDto.getQuestions().stream()
                                .map(QuestionResponse::toResponse
                                )
                                .toList()
                ).build();
    }
}