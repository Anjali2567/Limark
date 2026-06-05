package ai.leadplus.api.v1.admin.servicespecifications;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSpecificationRequest {

    @NotNull
    private Long serviceId;
    @NotNull
    private Long specificationId;
}
