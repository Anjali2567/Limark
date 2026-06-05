package ai.leadplus.api.v1.admin.specifications;

import ai.leadplus.application.specifications.SpecificationDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationRequest {

    @NotBlank
    private String name;
    private String type;
    private String icon;
    @NotNull
    private Long specificationCategoryId;

    public SpecificationDto toDto() {
        return SpecificationDto.builder()
                .name(name)
                .type(type)
                .icon(icon)
                .specificationCategoryId(specificationCategoryId)
                .disabled(false)
                .build();
    }
}
