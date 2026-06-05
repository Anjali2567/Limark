package ai.leadplus.infrastructure.hubspot.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HubSpotUserResponse {
    private String userId;
    private Long hubId;
    private String hubDomain;
    private String email;
}