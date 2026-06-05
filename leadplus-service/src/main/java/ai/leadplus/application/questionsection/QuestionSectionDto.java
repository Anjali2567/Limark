package ai.leadplus.application.questionsection;

import ai.leadplus.domain.questionsection.QuestionSection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSectionDto {

    private Long id;
    private String name;
    private Integer position;
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static QuestionSectionDto toDto(QuestionSection questionSection) {
        return QuestionSectionDto.builder()
                .id(questionSection.getId())
                .name(questionSection.getName())
                .position(questionSection.getPosition())
                .active(questionSection.isActive())
                .createdBy(questionSection.getCreatedBy())
                .createdAt(questionSection.getCreatedAt())
                .updatedBy(questionSection.getUpdatedBy())
                .updatedAt(questionSection.getUpdatedAt())
                .build();
    }

    public QuestionSection toEntity() {
        return QuestionSection.builder()
                .id(id)
                .name(name)
                .position(position)
                .active(active)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
