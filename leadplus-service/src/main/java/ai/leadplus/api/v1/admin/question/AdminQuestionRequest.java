package ai.leadplus.api.v1.admin.question;

import ai.leadplus.application.question.QuestionDto;
import ai.leadplus.domain.question.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AdminQuestionRequest {

    @NotBlank
    protected Long questionSectionId;
    protected Integer position;
    @NotNull
    protected QuestionType type;
    protected List<Long> industryIds;
    protected String label;
    protected List<String> options;

    public QuestionDto toDto() {
        return QuestionDto.builder()
                .questionSectionId(questionSectionId)
                .position(position)
                .type(type)
                .industryIds(industryIds)
                .label(label)
                .options(options)
                .build();
    }
}
