package ai.leadplus.infrastructure.apollo;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "apollo")
@ConditionalOnProperty(name = "apollo.enabled", havingValue = "true")
public class ApolloProperties {
    private String baseUrl;
    private String apiKey;
}
