package ai.leadplus.application.question;

import ai.leadplus.domain.question.Question;
import ai.leadplus.domain.question.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class QuestionDto {
    private Long id;
    private Long questionSectionId;
    private Integer position;
    private QuestionType type;
    private List<Long> industryIds;
    private String label;
    private List<String> options;
    private boolean active;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;

    public static QuestionDto toDto(Question question) {
        return QuestionDto.builder()
                .id(question.getId())
                .questionSectionId(question.getQuestionSectionId())
                .position(question.getPosition())
                .type(question.getType())
                .industryIds(question.getIndustryIds())
                .label(question.getLabel())
                .options(question.getOptions())
                .active(question.isActive())
                .createdAt(question.getCreatedAt())
                .createdBy(question.getCreatedBy())
                .updatedAt(question.getUpdatedAt())
                .updatedBy(question.getUpdatedBy())
                .build();
    }

    public Question toEntity() {
        return Question.builder()
                .id(id)
                .questionSectionId(questionSectionId)
                .position(position)
                .type(type)
                .industryIds(industryIds)
                .label(label)
                .options(options)
                .active(active)
                .createdAt(createdAt)
                .createdBy(createdBy)
                .updatedAt(updatedAt)
                .updatedBy(updatedBy)
                .build();
    }
}
