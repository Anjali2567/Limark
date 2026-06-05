package ai.leadplus.application.specifications;

import ai.leadplus.domain.specifications.Specification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationDto {

    private Long id;
    private String name;
    private String type;
    private String icon;
    private Long specificationCategoryId;
    private boolean disabled;
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static SpecificationDto fromEntity(Specification entity) {
        if (entity == null) return null;
        return SpecificationDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .icon(entity.getIcon())
                .specificationCategoryId(entity.getSpecificationCategoryId())
                .disabled(entity.isDisabled())
                .active(entity.isActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public Specification toEntity() {
        return Specification.builder()
                .id(id)
                .name(name)
                .type(type)
                .icon(icon)
                .specificationCategoryId(specificationCategoryId)
                .disabled(disabled)
                .active(active)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
