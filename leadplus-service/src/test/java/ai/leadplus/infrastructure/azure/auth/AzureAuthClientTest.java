package ai.leadplus.infrastructure.azure.auth;

import ai.leadplus.application.exception.ServiceUnavailableException;
import ai.leadplus.infrastructure.azure.AzureProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureAuthClientTest {

    @Mock
    private AzureProperties azureProperties;

    @Mock
    private AzureProperties.Authentication authentication;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AzureAuthClient azureAuthClient;

    private static final String REDIRECT_URI = "http://localhost:3000/leadgen/settings/communications/";

    @BeforeEach
    void setUp() {
        lenient().when(azureProperties.getAuthentication()).thenReturn(authentication);
        lenient().when(azureProperties.getTenantId()).thenReturn("tenant123");
        lenient().when(azureProperties.getClientId()).thenReturn("clientId123");
        lenient().when(azureProperties.getClientSecret()).thenReturn("secret123");
        lenient().when(authentication.getUrl()).thenReturn("https://login.microsoftonline.com");
        lenient().when(authentication.getScope()).thenReturn("user.read");
        lenient().when(azureProperties.getGraphBaseUrl()).thenReturn("https://graph.microsoft.com/v1.0");
    }


    @Test
    void fetchAccessTokenAndRefreshTokenFromCode_shouldReturnResponse() {
        String code = "authCode123";
        AzureRefreshTokenResponse mockResponse = new AzureRefreshTokenResponse();
        mockResponse.setAccess_token("access123");
        mockResponse.setRefresh_token("refresh123");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(AzureRefreshTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        AzureRefreshTokenResponse result = azureAuthClient.fetchAccessTokenAndRefreshTokenFromCode(code, REDIRECT_URI);

        assertThat(result).isNotNull();
        assertThat(result.getAccess_token()).isEqualTo("access123");
        assertThat(result.getRefresh_token()).isEqualTo("refresh123");

        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(contains("oauth2/v2.0/token"), requestCaptor.capture(), eq(AzureRefreshTokenResponse.class));

        MultiValueMap<String, String> body = requestCaptor.getValue().getBody();
        assertThat(body).isNotNull();
        assertThat(body.getFirst("code")).isEqualTo(code);
        assertThat(body.getFirst("redirect_uri")).isEqualTo(REDIRECT_URI);
        assertThat(body.getFirst("grant_type")).isEqualTo("authorization_code");
    }

    @Test
    void fetchAccessTokenAndRefreshTokenFromCode_whenResponseBodyNull_shouldThrowException() {
        String code = "authCode123";

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(AzureRefreshTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        assertThrows(ServiceUnavailableException.class,
                () -> azureAuthClient.fetchAccessTokenAndRefreshTokenFromCode(code, REDIRECT_URI));
    }

    @Test
    void fetchAccessTokenAndRefreshTokenFromRefreshToken_shouldReturnResponse() {
        String refreshToken = "refresh123";
        AzureRefreshTokenResponse mockResponse = new AzureRefreshTokenResponse();
        mockResponse.setAccess_token("access123");
        mockResponse.setRefresh_token("refresh123");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(AzureRefreshTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        AzureRefreshTokenResponse result = azureAuthClient.fetchAccessTokenAndRefreshTokenFromRefreshToken(refreshToken);

        assertThat(result).isNotNull();
        assertThat(result.getAccess_token()).isEqualTo("access123");
        assertThat(result.getRefresh_token()).isEqualTo("refresh123");

        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(contains("/common/oauth2/v2.0/token"), requestCaptor.capture(), eq(AzureRefreshTokenResponse.class));

        MultiValueMap<String, String> body = requestCaptor.getValue().getBody();
        assertThat(body).isNotNull();
        assertThat(body.getFirst("refresh_token")).isEqualTo(refreshToken);
        assertThat(body.getFirst("grant_type")).isEqualTo("refresh_token");
    }

    @Test
    void fetchAccessTokenAndRefreshTokenFromRefreshToken_whenResponseBodyNull_shouldThrowException() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(AzureRefreshTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        assertThrows(ServiceUnavailableException.class,
                () -> azureAuthClient.fetchAccessTokenAndRefreshTokenFromRefreshToken("refresh123"));
    }

    @Test
    void createHeader_shouldReturnHttpHeaders() {
        String accessToken = "token123";
        HttpHeaders headers = azureAuthClient.createHeader(accessToken);

        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer token123");
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    void createUserUrl_shouldReturnCorrectUrl() {
        String userId = "user123";
        String url = azureAuthClient.createUserUrl(userId);

        assertThat(url).isEqualTo("https://graph.microsoft.com/v1.0/users/user123");
    }
}
