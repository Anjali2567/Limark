package ai.leadplus.infrastructure.azure.graph.emails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AzureEmailAddressRequest {
    private String address;
    private String name;
}
