package ai.leadplus.infrastructure.aws.ses;

import ai.leadplus.application.exception.ServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AwsSESAuthClientTest {

    @Mock
    private SesClient sesClient;

    @InjectMocks
    private AwsSESAuthClient awsSESAuthClient;

    private final String testEmail = "test@example.com";

    @Test
    void sendVerificationEmail_shouldCallSesClient() {
        awsSESAuthClient.sendVerificationEmail(testEmail);

        ArgumentCaptor<VerifyEmailIdentityRequest> captor =
                ArgumentCaptor.forClass(VerifyEmailIdentityRequest.class);
        verify(sesClient).verifyEmailIdentity(captor.capture());

        VerifyEmailIdentityRequest request = captor.getValue();
        assertThat(request.emailAddress()).isEqualTo(testEmail);
    }

    @Test
    void sendVerificationEmail_whenSesThrows_shouldThrowServiceUnavailable() {
        doThrow(SesException.builder().message("AWS error").build())
                .when(sesClient).verifyEmailIdentity((VerifyEmailIdentityRequest) any());

        assertThrows(ServiceUnavailableException.class,
                () -> awsSESAuthClient.sendVerificationEmail(testEmail));
    }

    @Test
    void isEmailVerified_shouldReturnTrueWhenSuccess() {
        IdentityVerificationAttributes attributes = IdentityVerificationAttributes.builder()
                .verificationStatus(VerificationStatus.SUCCESS)
                .build();

        GetIdentityVerificationAttributesResponse response = GetIdentityVerificationAttributesResponse.builder()
                .verificationAttributes(Map.of(testEmail, attributes))
                .build();

        when(sesClient.getIdentityVerificationAttributes(any(GetIdentityVerificationAttributesRequest.class)))
                .thenReturn(response);

        boolean result = awsSESAuthClient.isEmailVerified(testEmail);

        assertThat(result).isTrue();

        ArgumentCaptor<GetIdentityVerificationAttributesRequest> captor =
                ArgumentCaptor.forClass(GetIdentityVerificationAttributesRequest.class);
        verify(sesClient).getIdentityVerificationAttributes(captor.capture());
        assertThat(captor.getValue().identities()).containsExactly(testEmail);
    }

    @Test
    void isEmailVerified_shouldReturnFalseWhenNotVerified() {
        IdentityVerificationAttributes attributes = IdentityVerificationAttributes.builder()
                .verificationStatus(VerificationStatus.FAILED)
                .build();

        GetIdentityVerificationAttributesResponse response = GetIdentityVerificationAttributesResponse.builder()
                .verificationAttributes(Map.of(testEmail, attributes))
                .build();

        when(sesClient.getIdentityVerificationAttributes(any(GetIdentityVerificationAttributesRequest.class)))
                .thenReturn(response);

        boolean result = awsSESAuthClient.isEmailVerified(testEmail);

        assertThat(result).isFalse();
    }

    @Test
    void isEmailVerified_shouldReturnFalseWhenAttributesNull() {
        GetIdentityVerificationAttributesResponse response = GetIdentityVerificationAttributesResponse.builder()
                .verificationAttributes(Map.of())
                .build();

        when(sesClient.getIdentityVerificationAttributes(any(GetIdentityVerificationAttributesRequest.class)))
                .thenReturn(response);

        boolean result = awsSESAuthClient.isEmailVerified(testEmail);

        assertThat(result).isFalse();
    }

    @Test
    void removeEmailIdentity_shouldCallSesClient() {
        awsSESAuthClient.removeEmailIdentity(testEmail);

        ArgumentCaptor<DeleteIdentityRequest> captor =
                ArgumentCaptor.forClass(DeleteIdentityRequest.class);
        verify(sesClient).deleteIdentity(captor.capture());

        DeleteIdentityRequest request = captor.getValue();
        assertThat(request.identity()).isEqualTo(testEmail);
    }

    @Test
    void removeEmailIdentity_whenSesThrows_shouldThrowServiceUnavailable() {
        doThrow(SesException.builder().message("AWS error").build())
                .when(sesClient).deleteIdentity((DeleteIdentityRequest) any());

        assertThrows(ServiceUnavailableException.class,
                () -> awsSESAuthClient.removeEmailIdentity(testEmail));
    }
}

