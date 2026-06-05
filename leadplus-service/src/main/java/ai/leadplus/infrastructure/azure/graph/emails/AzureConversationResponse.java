package ai.leadplus.infrastructure.azure.graph.emails;

import lombok.Data;
import java.util.List;

@Data
public class AzureConversationResponse {
    private List<AzureGraphMessageResponse> value;
}
