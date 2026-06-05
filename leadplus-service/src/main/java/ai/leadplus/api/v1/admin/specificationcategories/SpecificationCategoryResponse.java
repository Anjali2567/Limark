package ai.leadplus.api.v1.admin.specificationcategories;

import ai.leadplus.application.specificationcategories.SpecificationCategoryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationCategoryResponse {

    private Long id;
    private String name;
    private String type;
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static SpecificationCategoryResponse fromDto(SpecificationCategoryDto dto) {
        if (dto == null) return null;
        return SpecificationCategoryResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .type(dto.getType())
                .active(dto.isActive())
                .createdBy(dto.getCreatedBy())
                .createdAt(dto.getCreatedAt())
                .updatedBy(dto.getUpdatedBy())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
