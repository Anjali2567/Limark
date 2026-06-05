package ai.leadplus.application.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SectionWithQuestionDto {
    private String sectionId;
    private String sectionName;
    private Integer position;
    private List<QuestionDto> questions;
}
