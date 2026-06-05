package ai.leadplus.infrastructure.azure;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "azure")
public class AzureProperties {
    private String clientId;
    private String clientSecret;
    private String tenantId;
    private String graphBaseUrl;

    @NestedConfigurationProperty
    private Authentication authentication;

    @Data
    public static class Authentication {
        private String url;
        private String scope;
        private String redirectUri;
    }
}
