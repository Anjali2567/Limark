package ai.leadplus.api.v1.admin.services;

import ai.leadplus.application.services.ServiceDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {

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

    public static ServiceResponse fromDto(ServiceDto dto) {
        if (dto == null) return null;
        return ServiceResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .slug(dto.getSlug())
                .serviceCategoryId(dto.getServiceCategoryId())
                .disabled(dto.isDisabled())
                .active(dto.isActive())
                .createdBy(dto.getCreatedBy())
                .createdAt(dto.getCreatedAt())
                .updatedBy(dto.getUpdatedBy())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
