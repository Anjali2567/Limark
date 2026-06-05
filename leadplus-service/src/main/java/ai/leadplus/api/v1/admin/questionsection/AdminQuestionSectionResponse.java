package ai.leadplus.api.v1.admin.questionsection;

import ai.leadplus.application.questionsection.QuestionSectionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminQuestionSectionResponse {

    private Long id;
    private String name;
    private Integer position;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static AdminQuestionSectionResponse toResponse(QuestionSectionDto questionSectionDto) {
        return AdminQuestionSectionResponse.builder()
                .id(questionSectionDto.getId())
                .name(questionSectionDto.getName())
                .position(questionSectionDto.getPosition())
                .createdBy(questionSectionDto.getCreatedBy())
                .createdAt(questionSectionDto.getCreatedAt())
                .updatedBy(questionSectionDto.getUpdatedBy())
                .updatedAt(questionSectionDto.getUpdatedAt())
                .build();
    }

}
