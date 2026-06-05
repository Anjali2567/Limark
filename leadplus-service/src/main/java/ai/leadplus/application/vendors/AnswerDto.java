package ai.leadplus.application.vendors;

import ai.leadplus.domain.vendors.Answer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDto {

    private Long questionId;
    private String answer;

    public static AnswerDto fromEntity(Answer answer) {
        if (answer == null) {
            return null;
        }
        return AnswerDto.builder()
                .questionId(answer.getQuestionId())
                .answer(answer.getAnswer())
                .build();
    }

    public Answer toEntity() {
        return Answer.builder()
                .questionId(questionId)
                .answer(answer)
                .build();
    }
}
