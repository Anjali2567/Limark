package ai.leadplus.api.v1.admin.industryservices;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryServiceRequest {

    @NotBlank
    private Long industryId;
    @NotBlank
    private Long serviceId;
}
