package ai.leadplus.api.v1.admin.questionsection;

import ai.leadplus.application.questionsection.QuestionSectionDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AdminQuestionSectionRequest {

    @NotBlank(message = "Section name cannot be blank")
    protected String name;
    protected Integer position;

    public QuestionSectionDto toDto() {
        return QuestionSectionDto.builder()
                .name(name)
                .position(position)
                .build();
    }
}
