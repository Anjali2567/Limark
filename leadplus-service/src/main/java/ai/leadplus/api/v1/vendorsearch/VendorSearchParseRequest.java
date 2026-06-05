package ai.leadplus.api.v1.vendorsearch;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VendorSearchParseRequest {
    @NotBlank(message = "User input is mandatory")
    private String prompt;
}
