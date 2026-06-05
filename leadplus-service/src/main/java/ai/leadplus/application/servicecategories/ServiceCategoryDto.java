package ai.leadplus.application.servicecategories;

import ai.leadplus.domain.servicecategories.ServiceCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCategoryDto {

    private Long id;
    private String name;
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static ServiceCategoryDto fromEntity(ServiceCategory entity) {
        if (entity == null) return null;
        return ServiceCategoryDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .active(entity.isActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ServiceCategory toEntity() {
        return ServiceCategory.builder()
                .id(id)
                .name(name)
                .active(active)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
