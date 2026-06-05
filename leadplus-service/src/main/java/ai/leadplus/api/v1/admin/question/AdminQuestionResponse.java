package ai.leadplus.api.v1.admin.question;

import ai.leadplus.application.question.QuestionDto;
import ai.leadplus.domain.question.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminQuestionResponse {

    private Long id;
    private Long questionSectionId;
    private Integer position;
    private List<Long> industryIds;
    private QuestionType type;
    private String label;
    private List<String> options;

    public static AdminQuestionResponse toResponse(QuestionDto questionDto) {
        return AdminQuestionResponse.builder()
                .id(questionDto.getId())
                .questionSectionId(questionDto.getQuestionSectionId())
                .position(questionDto.getPosition())
                .industryIds(questionDto.getIndustryIds())
                .type(questionDto.getType())
                .label(questionDto.getLabel())
                .options(questionDto.getOptions())
                .build();
    }
}
