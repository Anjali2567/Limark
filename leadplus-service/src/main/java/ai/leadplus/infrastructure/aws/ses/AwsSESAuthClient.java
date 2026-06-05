package ai.leadplus.infrastructure.aws.ses;

import ai.leadplus.application.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsSESAuthClient {

    private final SesClient sesClient;

    public void sendVerificationEmail(String emailAddress) {
        try {
            VerifyEmailIdentityRequest request = VerifyEmailIdentityRequest.builder()
                    .emailAddress(emailAddress)
                    .build();
            sesClient.verifyEmailIdentity(request);
            log.info("Verification email has been sent to {}", emailAddress);
        } catch (SesException e) {
            throw new ServiceUnavailableException("Failed to verify email: " + e);
        }
    }

    public boolean isEmailVerified(String emailAddress) {
        GetIdentityVerificationAttributesRequest request =
                GetIdentityVerificationAttributesRequest.builder()
                        .identities(emailAddress)
                        .build();

        GetIdentityVerificationAttributesResponse response =
                sesClient.getIdentityVerificationAttributes(request);

        IdentityVerificationAttributes identityVerificationAttributes =  response.verificationAttributes()
                .get(emailAddress);
        boolean isVerified =  (identityVerificationAttributes != null) && (identityVerificationAttributes
                .verificationStatus()
                .equals(VerificationStatus.SUCCESS));
        log.info("Email {} verification status: {}", emailAddress, isVerified);
        return isVerified;
    }

    public void removeEmailIdentity(String emailAddress) {
        try {
            DeleteIdentityRequest deleteRequest = DeleteIdentityRequest.builder()
                    .identity(emailAddress)
                    .build();
            sesClient.deleteIdentity(deleteRequest);
            log.info("Email identity {} has been removed from SES", emailAddress);
        } catch (SesException e) {
            throw new ServiceUnavailableException("Failed to remove email identity: " + e);
        }
    }

}

