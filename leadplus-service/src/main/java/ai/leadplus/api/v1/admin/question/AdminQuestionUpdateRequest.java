package ai.leadplus.api.v1.admin.question;

import ai.leadplus.application.question.QuestionDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminQuestionUpdateRequest extends AdminQuestionRequest {
    private Long id;

    public QuestionDto toDto() {
        return QuestionDto.builder()
                .id(id)
                .questionSectionId(questionSectionId)
                .position(position)
                .type(type)
                .industryIds(industryIds)
                .label(label)
                .options(options)
                .build();
    }
}
