package ai.leadplus.infrastructure.aws.ses;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ServiceUnavailableException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AwsSESClientTest {

    @Mock
    private SesClient sesClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AwsSESClient awsSESClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(awsSESClient, "senderEmail", "sender@example.com");
        ReflectionTestUtils.setField(awsSESClient, "baseUrl", "https://base.url");
    }

    @Test
    void sendEmail_shouldCallSesClient() throws Exception {
        List<String> recipients = List.of("test@example.com");
        String template = "welcome_template";

        // use mutable map
        Map<String, Object> data = new HashMap<>();
        data.put("NAME", "John");

        when(objectMapper.writeValueAsString(anyMap()))
                .thenReturn("{\"NAME\":\"John\",\"BASE_URL\":\"https://base.url\"}");

        awsSESClient.sendEmail(recipients, template, data);

        ArgumentCaptor<SendTemplatedEmailRequest> captor = ArgumentCaptor.forClass(SendTemplatedEmailRequest.class);
        verify(sesClient).sendTemplatedEmail(captor.capture());

        SendTemplatedEmailRequest request = captor.getValue();
        assertThat(request.source()).isEqualTo("LeadPlus <sender@example.com>");
        assertThat(request.template()).isEqualTo(template);
        assertThat(request.destination().toAddresses()).containsExactly("test@example.com");
        assertThat(request.templateData()).contains("BASE_URL");
    }

    @Test
    void sendEmail_whenRecipientEmpty_shouldThrowBadRequest() {
        assertThrows(BadRequestException.class,
                () -> awsSESClient.sendEmail(List.of(), "template", Map.of()));
    }

    @Test
    void sendEmail_whenTemplateEmpty_shouldThrowBadRequest() {
        assertThrows(BadRequestException.class,
                () -> awsSESClient.sendEmail(List.of("a@b.com"), "", Map.of()));
    }

    @Test
    void sendEmail_whenTemplateDataNull_shouldThrowBadRequest() {
        assertThrows(BadRequestException.class,
                () -> awsSESClient.sendEmail(List.of("a@b.com"), "template", null));
    }

    @Test
    void sendEmail_whenObjectMapperFails_shouldThrowServiceUnavailable() throws JsonProcessingException {
        List<String> recipients = List.of("test@example.com");
        String template = "welcome_template";

        Map<String, Object> data = new HashMap<>();
        data.put("NAME", "John");

        when(objectMapper.writeValueAsString(anyMap())).thenThrow(new RuntimeException("Serialization failed"));

        assertThrows(ServiceUnavailableException.class,
                () -> awsSESClient.sendEmail(recipients, template, data));
    }

    @Test
    void sendEmailWithAttachments_shouldCallSesClient() {
        AwsSimpleEmailRequest request = AwsSimpleEmailRequest.builder()
                .senderName("Sender")
                .senderEmail("sender@example.com")
                .toRecipients(List.of("to@example.com"))
                .subject("Subject")
                .body("Body")
                .build();

        awsSESClient.sendEmailWithAttachments(request);

        ArgumentCaptor<SendRawEmailRequest> captor = ArgumentCaptor.forClass(SendRawEmailRequest.class);
        verify(sesClient).sendRawEmail(captor.capture());

        SendRawEmailRequest rawRequest = captor.getValue();
        assertThat(rawRequest.rawMessage().data().asByteArray()).isNotEmpty();
    }

    @Test
    void sendEmailWithAttachments_missingSender_shouldThrowBadRequest() {
        AwsSimpleEmailRequest request = AwsSimpleEmailRequest.builder()
                .toRecipients(List.of("to@example.com"))
                .subject("Subject")
                .body("Body")
                .build();

        assertThrows(BadRequestException.class,
                () -> awsSESClient.sendEmailWithAttachments(request));
    }

    @Test
    void sendEmailWithAttachments_missingRecipients_shouldThrowBadRequest() {
        AwsSimpleEmailRequest request = AwsSimpleEmailRequest.builder()
                .senderEmail("sender@example.com")
                .subject("Subject")
                .body("Body")
                .build();

        assertThrows(BadRequestException.class,
                () -> awsSESClient.sendEmailWithAttachments(request));
    }

    @Test
    void sendEmailWithAttachments_whenSesThrows_shouldThrowServiceUnavailable() {
        AwsSimpleEmailRequest request = AwsSimpleEmailRequest.builder()
                .senderName("Sender")
                .senderEmail("sender@example.com")
                .toRecipients(List.of("to@example.com"))
                .subject("Subject")
                .body("Body")
                .build();

        doThrow(RuntimeException.class).when(sesClient).sendRawEmail((SendRawEmailRequest) any());

        assertThrows(ServiceUnavailableException.class,
                () -> awsSESClient.sendEmailWithAttachments(request));
    }
}
