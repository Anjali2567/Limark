package ai.leadplus.application.auth;

import ai.leadplus.application.aws.EmailService;
import ai.leadplus.application.mailboxes.MailboxDto;
import ai.leadplus.application.mailboxes.MailboxProviderConfigDto;
import ai.leadplus.application.mailboxes.MailboxService;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.domain.mailboxes.AwsSESVerificationState;
import ai.leadplus.infrastructure.aws.ses.AwsSESAuthClient;
import ai.leadplus.infrastructure.azure.auth.AzureAuthClient;
import ai.leadplus.infrastructure.azure.auth.AzureRefreshTokenResponse;
import ai.leadplus.infrastructure.azure.graph.users.AzureUserClient;
import ai.leadplus.infrastructure.azure.graph.users.AzureUserResponse;
import ai.leadplus.infrastructure.google.auth.GoogleAuthClient;
import ai.leadplus.infrastructure.google.auth.GoogleTokenResponse;
import ai.leadplus.infrastructure.google.label.GmailLabelClient;
import ai.leadplus.infrastructure.google.userinfo.GoogleUserInfoClient;
import ai.leadplus.infrastructure.google.userinfo.GoogleUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements AuditorAware<Long> {

    private final AzureAuthClient azureAuthClient;
    private final AzureUserClient azureUserClient;
    private final AwsSESAuthClient awsSESAuthClient;
    private final MailboxService mailboxService;
    private final GoogleAuthClient googleAuthClient;
    private final GoogleUserInfoClient googleUserClient;
    private final EmailService emailService;
    private final UserService userService;
    private final GmailLabelClient gmailLabelClient;

    @Value("${client.url}")
    private String clientUrl;

    public void authenticateMicrosoft(Long userId, Long workspaceId, String code, String redirectUri) {
        AzureRefreshTokenResponse response = azureAuthClient.fetchAccessTokenAndRefreshTokenFromCode(code, redirectUri);
        AzureUserResponse userResponse = azureUserClient.getUserDetails(response.getAccess_token());
        mailboxService.updateAzureDetails(userId,
                workspaceId,
                userResponse.getId(),
                userResponse.getMail(),
                response.getRefresh_token()
        );
    }

    public void authenticateGoogle(Long userId, Long workspaceId, String code, String redirectUri) {
        GoogleTokenResponse response = googleAuthClient.fetchTokensFromCode(code, redirectUri);
        GoogleUserInfoResponse userResponse = googleUserClient.getUserInfo(response.getAccess_token());
        String labelId = gmailLabelClient.getOrCreateLeadPlusLabelId(response.getAccess_token());
        mailboxService.updateGoogleDetails(userId,
                workspaceId,
                userResponse.getId(),
                userResponse.getEmail(),
                response.getRefresh_token(),
                labelId
        );
    }

    public MailboxDto sendAwsSESVerificationEmail(UserDto userDto, Long workspaceId, String email) {

        MailboxDto mailboxDto =
                mailboxService.getOrCreateSesDetails(userDto.getId(), email, workspaceId);

        if (mailboxDto.getMetaData() == null) {
            mailboxDto.setMetaData(MailboxProviderConfigDto.builder()
                    .awsSESVerificationState(AwsSESVerificationState.UNVERIFIED)
                    .build());
        }

        AwsSESVerificationState state = mailboxDto.getMetaData().getAwsSESVerificationState();

        if (state == AwsSESVerificationState.VERIFIED) {
            return mailboxDto;
        }

        if (state == AwsSESVerificationState.PENDING) {
            boolean isVerified = awsSESAuthClient.isEmailVerified(email);

            if (isVerified) {
                mailboxDto.getMetaData()
                        .setAwsSESVerificationState(AwsSESVerificationState.VERIFIED);
                mailboxService.saveMailbox(mailboxDto);
            }

            return mailboxDto;
        }

        awsSESAuthClient.sendVerificationEmail(email);
        mailboxDto.getMetaData()
                .setAwsSESVerificationState(AwsSESVerificationState.PENDING);
        mailboxService.saveMailbox(mailboxDto);

        return mailboxDto;
    }

    public void sendForgotPasswordLink(String userEmail) {
        String verificationToken = UUID.randomUUID().toString();
        UserDto user = userService.getUserByEmail(userEmail);
        user.setVerificationToken(verificationToken);
        userService.saveUser(user);

        String verificationLink = String.format(
                "%s/password-reset?token=%s",
                clientUrl, verificationToken);

        emailService.sendResetPasswordLink(user.getEmail(), user.getName(), verificationLink);
    }

    public UserDto verifyForgotPassword(ForgotPasswordDto forgotPasswordDto) {
        return userService.verifyForgotPassword(forgotPasswordDto);
    }

    @NonNull
    @Override
    public Optional<Long> getCurrentAuditor() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .filter(name -> name != null && name.matches("\\d+"))
                .map(Long::parseLong);
    }

    public void verifyEmail(String token) {
        UserDto user = userService.getByEmailVerificationToken(token);
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        userService.saveUser(user);
    }
}
