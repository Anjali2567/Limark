package ai.leadplus.infrastructure.hubspot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "hubspot")
public class HubSpotConfiguration {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String apiDomain;
}