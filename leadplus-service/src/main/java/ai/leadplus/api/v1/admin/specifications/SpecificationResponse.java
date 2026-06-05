package ai.leadplus.api.v1.admin.specifications;

import ai.leadplus.application.specifications.SpecificationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationResponse {

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

    public static SpecificationResponse fromDto(SpecificationDto dto) {
        if (dto == null) return null;
        return SpecificationResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .type(dto.getType())
                .icon(dto.getIcon())
                .specificationCategoryId(dto.getSpecificationCategoryId())
                .disabled(dto.isDisabled())
                .active(dto.isActive())
                .createdBy(dto.getCreatedBy())
                .createdAt(dto.getCreatedAt())
                .updatedBy(dto.getUpdatedBy())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
