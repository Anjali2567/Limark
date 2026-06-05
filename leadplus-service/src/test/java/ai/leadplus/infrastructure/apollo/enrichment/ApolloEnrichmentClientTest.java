package ai.leadplus.infrastructure.apollo.enrichment;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.infrastructure.apollo.ApolloProperties;
import ai.leadplus.infrastructure.apollo.ApolloUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApolloEnrichmentClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApolloUtilities apolloUtilities;

    private ApolloEnrichmentClient client;

    @BeforeEach
    void setUp() {
        ApolloProperties apolloProperties = new ApolloProperties();
        apolloProperties.setBaseUrl("https://api.apollo.io");

        client = new ApolloEnrichmentClient(apolloProperties, apolloUtilities, restTemplate);

        lenient().when(apolloUtilities.createHeader()).thenReturn(new HttpHeaders());
    }

    @Test
    void enrichByApolloId_shouldReturnResponse() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("mocked response"));

        String response = client.enrichByApolloId("apollo123");

        assertThat(response).isEqualTo("mocked response");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), any(HttpEntity.class), eq(String.class));

        String url = urlCaptor.getValue();
        assertThat(url).contains("/people/match?id=apollo123");
    }

    @Test
    void bulkEnrichByApolloId_shouldReturnResponse() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("bulk response"));

        String response = client.bulkEnrichByApolloId(List.of("1", "2"));

        assertThat(response).isEqualTo("bulk response");

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        verify(restTemplate).postForEntity(urlCaptor.capture(), entityCaptor.capture(), eq(String.class));

        String url = urlCaptor.getValue();
        assertThat(url).contains("/people/bulk_match");

        HttpEntity<?> entity = entityCaptor.getValue();
        assertThat(entity.getBody()).isInstanceOf(ApolloEnrichmentBulkRequest.class);

        ApolloEnrichmentBulkRequest request = (ApolloEnrichmentBulkRequest) entity.getBody();
        Assertions.assertNotNull(request);
        assertThat(request.getDetails()).hasSize(2);
        assertThat(request.getDetails().get(0).getId()).isEqualTo("1");
        assertThat(request.getDetails().get(1).getId()).isEqualTo("2");
    }

    @Test
    void enrichByApolloId_shouldThrow_whenApolloIdIsNull() {
        Assertions.assertThrows(BadRequestException.class, () -> client.enrichByApolloId(null));
    }

    @Test
    void bulkEnrichByApolloId_shouldThrow_whenListIsEmpty() {
        Assertions.assertThrows(BadRequestException.class, () -> client.bulkEnrichByApolloId(List.of()));
    }

    @Test
    void enrichByApolloId_shouldThrow_whenRestTemplateFails() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("API failure"));

        Assertions.assertThrows(RuntimeException.class, () -> client.enrichByApolloId("apollo123"));
    }

    @Test
    void bulkEnrichByApolloId_shouldThrow_whenRestTemplateFails() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("API failure"));

        Assertions.assertThrows(RuntimeException.class, () -> client.bulkEnrichByApolloId(List.of("1", "2")));
    }

}
