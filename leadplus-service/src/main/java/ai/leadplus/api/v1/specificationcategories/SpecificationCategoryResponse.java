package ai.leadplus.api.v1.specificationcategories;

import ai.leadplus.application.specificationcategories.SpecificationCategoryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationCategoryResponse {

    private Long id;
    private String name;
    private String type;

    public static SpecificationCategoryResponse fromDto(SpecificationCategoryDto dto) {
        if (dto == null) return null;
        return SpecificationCategoryResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .type(dto.getType())
                .build();
    }
}
