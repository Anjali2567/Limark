package ai.leadplus.infrastructure.apollo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ApolloUtilitiesTest {

    private ApolloUtilities apolloUtilities;

    @BeforeEach
    void setUp() {
        ApolloProperties apolloProperties = new ApolloProperties();
        apolloProperties.setApiKey("test-api-key");
        apolloUtilities = new ApolloUtilities(apolloProperties);
    }

    @Test
    void createHeader_shouldSetAllHeadersCorrectly() {
        HttpHeaders headers = apolloUtilities.createHeader();

        assertThat(headers.getFirst("x-api-key")).isEqualTo("test-api-key");
        assertThat(headers.getCacheControl()).isEqualTo("no-cache");
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(headers.getAccept()).containsExactly(MediaType.APPLICATION_JSON);
    }
}