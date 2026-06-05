package ai.leadplus.application.services;

import ai.leadplus.domain.services.Service;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDto {

    private Long id;
    private String name;
    private String slug;
    private Long serviceCategoryId;
    private boolean disabled;
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static ServiceDto fromEntity(Service entity) {
        if (entity == null) return null;
        return ServiceDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .serviceCategoryId(entity.getServiceCategoryId())
                .disabled(entity.isDisabled())
                .active(entity.isActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public Service toEntity() {
        return Service.builder()
                .id(id)
                .name(name)
                .slug(slug)
                .serviceCategoryId(serviceCategoryId)
                .disabled(disabled)
                .active(active)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
