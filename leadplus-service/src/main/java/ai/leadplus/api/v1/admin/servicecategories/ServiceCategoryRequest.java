package ai.leadplus.api.v1.admin.servicecategories;

import ai.leadplus.application.servicecategories.ServiceCategoryDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCategoryRequest {

    @NotBlank
    private String name;

    public ServiceCategoryDto toDto() {
        return ServiceCategoryDto.builder()
                .name(name)
                .build();
    }
}
