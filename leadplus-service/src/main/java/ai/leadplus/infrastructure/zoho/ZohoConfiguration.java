package ai.leadplus.infrastructure.zoho;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "zoho")
public class ZohoConfiguration {
  private String clientId;
  private String clientSecret;
  private String accountsDomain;
  private String apiDomain;
  private String redirectUri;
}
