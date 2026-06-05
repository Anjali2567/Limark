package ai.leadplus.infrastructure.apollo;

import ai.leadplus.application.apollospecification.ApolloSpecificationDto;
import ai.leadplus.domain.apollospecification.ApolloSpecification;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApolloSearchClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApolloUtilities apolloUtilities;

    private ApolloSearchClient apolloSearchClient;

    @BeforeEach
    void setUp() {
        ApolloProperties apolloProperties = new ApolloProperties();
        apolloProperties.setBaseUrl("https://api.apollo.io");

        apolloSearchClient =
                new ApolloSearchClient(apolloProperties, restTemplate, apolloUtilities);

        HttpHeaders headers = new HttpHeaders();
        lenient().when(apolloUtilities.createHeader()).thenReturn(headers);
    }

    @Test
    void searchPeople_shouldReturnResponseBody() {
        ApolloSpecificationDto specificationDto = ApolloSpecificationDto.builder()
                .personTitleEnabled(true)
                .personTitles(List.of("CEO"))
                .personSeniorityEnabled(true)
                .personSeniorities(List.of("Executive"))
                .build();

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("mocked response"));

        String response = apolloSearchClient.searchPeople("example.com", specificationDto);

        assertThat(response).isEqualTo("mocked response");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), any(HttpEntity.class), eq(String.class));

        String builtUrl = urlCaptor.getValue();
        assertThat(builtUrl).contains("person_titles[]=CEO");
        assertThat(builtUrl).contains("person_seniorities[]=Executive");
        assertThat(builtUrl).contains("q_organization_domains_list[]=example.com");
    }

    @Test
    void searchPeople_shouldHandleEmptyTitlesAndSeniorities() {
        ApolloSpecificationDto specificationDto = ApolloSpecificationDto.builder()
                .personTitleEnabled(false)
                .personTitles(List.of())
                .personSeniorityEnabled(false)
                .personSeniorities(List.of())
                .build();

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("mocked response"));

        String response = apolloSearchClient.searchPeople("example.com", specificationDto);

        assertThat(response).isEqualTo("mocked response");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), any(HttpEntity.class), eq(String.class));

        String builtUrl = urlCaptor.getValue();
        assertThat(builtUrl).doesNotContain("person_titles[]");
        assertThat(builtUrl).doesNotContain("person_seniorities[]");
        assertThat(builtUrl).contains("q_organization_domains_list[]=example.com");
    }

    @Test
    void searchPeople_shouldThrow_whenRestTemplateFails() {
        ApolloSpecificationDto specificationDto = ApolloSpecificationDto.builder()
                .personTitleEnabled(true)
                .personTitles(List.of("CEO"))
                .personSeniorityEnabled(true)
                .personSeniorities(List.of("Executive"))
                .build();

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("API failure"));

        assertThrows(RuntimeException.class, () ->
                apolloSearchClient.searchPeople("example.com", specificationDto));
    }

    @Test
    void dtoConversion_toAndFromSpecification_shouldWork() {
        ApolloSpecification spec = ApolloSpecification.builder()
                .id(1L)
                .personTitleEnabled(true)
                .personTitles(List.of("CEO"))
                .personSeniorityEnabled(true)
                .personSeniorities(List.of("Executive"))
                .build();

        ApolloSpecificationDto dto = ApolloSpecificationDto.fromSpecification(spec);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.isPersonTitleEnabled()).isTrue();
        assertThat(dto.getPersonTitles()).containsExactly("CEO");
        assertThat(dto.isPersonSeniorityEnabled()).isTrue();
        assertThat(dto.getPersonSeniorities()).containsExactly("Executive");

        ApolloSpecification converted = dto.toSpecification();
        assertThat(converted.getId()).isEqualTo(1L);
        assertThat(converted.isPersonTitleEnabled()).isTrue();
        assertThat(converted.getPersonTitles()).containsExactly("CEO");
        assertThat(converted.isPersonSeniorityEnabled()).isTrue();
        assertThat(converted.getPersonSeniorities()).containsExactly("Executive");
    }

    @Test
    void dtoConversion_fromSpecification_shouldReturnNullForNullInput() {
        ApolloSpecificationDto dto = ApolloSpecificationDto.fromSpecification(null);
        assertThat(dto).isNull();
    }
}
