package ai.leadplus.api.v1.questionnaires;

import ai.leadplus.application.question.QuestionDto;
import ai.leadplus.domain.question.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponse {
    private Long id;
    private Long questionSectionId;
    private Integer position;
    private QuestionType type;
    private String label;
    private List<String> options;

    public static QuestionResponse toResponse(QuestionDto questionDto) {
        return QuestionResponse.builder()
                .id(questionDto.getId())
                .questionSectionId(questionDto.getQuestionSectionId())
                .position(questionDto.getPosition())
                .type(questionDto.getType())
                .label(questionDto.getLabel())
                .options(questionDto.getOptions())
                .build();
    }
}
