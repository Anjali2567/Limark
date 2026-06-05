package ai.leadplus.api.v1.admin.services;

import ai.leadplus.application.services.ServiceDto;
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
public class ServiceRequest {

    @NotBlank
    private String name;
    @NotNull
    private Long serviceCategoryId;

    public ServiceDto toDto() {
        return ServiceDto.builder()
                .name(name)
                .serviceCategoryId(serviceCategoryId)
                .disabled(false)
                .build();
    }
}
