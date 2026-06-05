package ai.leadplus.api.v1.admin.servicecategories;

import ai.leadplus.application.servicecategories.ServiceCategoryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCategoryResponse {

    private Long id;
    private String name;
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static ServiceCategoryResponse fromDto(ServiceCategoryDto dto) {
        if (dto == null) return null;
        return ServiceCategoryResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .active(dto.isActive())
                .createdBy(dto.getCreatedBy())
                .createdAt(dto.getCreatedAt())
                .updatedBy(dto.getUpdatedBy())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
