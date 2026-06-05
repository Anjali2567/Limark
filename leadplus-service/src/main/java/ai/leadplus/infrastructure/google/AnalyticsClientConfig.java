package ai.leadplus.infrastructure.google;

import ai.leadplus.application.exception.AnalyticsException;
import com.google.analytics.data.v1beta.BetaAnalyticsDataClient;
import com.google.analytics.data.v1beta.BetaAnalyticsDataSettings;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AnalyticsClientConfig {

    private final GoogleAnalyticsCredentialsProperties credentialsProperties;

    @Bean(destroyMethod = "close")
    public BetaAnalyticsDataClient betaAnalyticsDataClient() {
        try {
            String rawKey = credentialsProperties.getPrivateKey()
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(rawKey);
            PrivateKey privateKey = KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));

            GoogleCredentials credentials = ServiceAccountCredentials.newBuilder()
                    .setClientEmail(credentialsProperties.getClientEmail())
                    .setClientId(credentialsProperties.getClientId())
                    .setPrivateKey(privateKey)
                    .setPrivateKeyId(credentialsProperties.getPrivateKeyId())
                    .setTokenServerUri(new java.net.URI(credentialsProperties.getTokenUri()).toURL().toURI())
                    .setScopes(Collections.singletonList(
                            "https://www.googleapis.com/auth/analytics.readonly"))
                    .build();

            return BetaAnalyticsDataClient.create(
                    BetaAnalyticsDataSettings.newBuilder()
                            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to initialize Google Analytics client", e);
            throw new AnalyticsException("Error initializing Google Analytics client.", e);
        }
    }
}