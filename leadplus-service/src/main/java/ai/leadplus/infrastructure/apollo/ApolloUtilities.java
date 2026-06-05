package ai.leadplus.infrastructure.apollo;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "apollo.enabled", havingValue = "true")
public class ApolloUtilities {

    private final ApolloProperties apolloProperties;

    public HttpHeaders createHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apolloProperties.getApiKey());
        headers.setCacheControl("no-cache");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
