package ai.leadplus.api.v1.services;

import ai.leadplus.application.services.ServiceDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {

    private Long id;
    private String name;
    private String slug;
    private Long serviceCategoryId;

    public static ServiceResponse fromDto(ServiceDto dto) {
        if (dto == null) return null;
        return ServiceResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .slug(dto.getSlug())
                .serviceCategoryId(dto.getServiceCategoryId())
                .build();
    }
}
