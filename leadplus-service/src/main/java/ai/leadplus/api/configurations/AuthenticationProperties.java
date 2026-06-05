package ai.leadplus.api.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "authentication")
public class AuthenticationProperties {
    private String apikey;
    private String leadApiKey;
}