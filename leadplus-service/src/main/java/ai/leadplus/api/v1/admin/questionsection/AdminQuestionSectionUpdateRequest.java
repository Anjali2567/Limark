package ai.leadplus.api.v1.admin.questionsection;

import ai.leadplus.application.questionsection.QuestionSectionDto;
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
public class AdminQuestionSectionUpdateRequest extends AdminQuestionSectionRequest {

    private Long id;

    public QuestionSectionDto toDto() {
        return QuestionSectionDto.builder()
                .id(id)
                .position(position)
                .name(name)
                .build();
    }
}