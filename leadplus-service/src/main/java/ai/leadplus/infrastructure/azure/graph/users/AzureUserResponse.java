package ai.leadplus.infrastructure.azure.graph.users;

import lombok.Data;

@Data
public class AzureUserResponse {
    private String id;
    private String displayName;
    private String mail;
}
