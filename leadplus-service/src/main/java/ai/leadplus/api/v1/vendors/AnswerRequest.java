package ai.leadplus.api.v1.vendors;

import ai.leadplus.application.vendors.AnswerDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequest {
    @NotNull
    private Long questionId;
    private String answer;

     public AnswerDto toDto() {
        return AnswerDto.builder()
                .questionId(questionId)
                .answer(answer)
                .build();
    }
}
