package ai.leadplus.application.specificationcategories;

import ai.leadplus.domain.specificationcategories.SpecificationCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationCategoryDto {

    private Long id;
    private String name;
    private String type;
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static SpecificationCategoryDto fromEntity(SpecificationCategory entity) {
        if (entity == null) return null;
        return SpecificationCategoryDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .active(entity.isActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public SpecificationCategory toEntity() {
        return SpecificationCategory.builder()
                .id(id)
                .name(name)
                .type(type)
                .active(active)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
