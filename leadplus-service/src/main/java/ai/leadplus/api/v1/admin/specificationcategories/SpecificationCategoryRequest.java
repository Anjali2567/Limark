package ai.leadplus.api.v1.admin.specificationcategories;

import ai.leadplus.application.specificationcategories.SpecificationCategoryDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationCategoryRequest {

    @NotBlank
    private String name;
    private String type;

    public SpecificationCategoryDto toDto() {
        return SpecificationCategoryDto.builder()
                .name(name)
                .type(type)
                .build();
    }
}
