package ai.leadplus.api.v1.servicecategories;

import ai.leadplus.application.servicecategories.ServiceCategoryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCategoryResponse {

    private Long id;
    private String name;

    public static ServiceCategoryResponse fromDto(ServiceCategoryDto dto) {
        if (dto == null) return null;
        return ServiceCategoryResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}
