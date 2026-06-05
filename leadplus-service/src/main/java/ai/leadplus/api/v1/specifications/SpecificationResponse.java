package ai.leadplus.api.v1.specifications;

import ai.leadplus.application.specifications.SpecificationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public static SpecificationResponse fromDto(SpecificationDto dto) {
        if (dto == null) return null;
        return SpecificationResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .type(dto.getType())
                .icon(dto.getIcon())
                .specificationCategoryId(dto.getSpecificationCategoryId())
                .build();
    }
}
