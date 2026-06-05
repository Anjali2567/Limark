package ai.leadplus.application.mailboxes;

import ai.leadplus.domain.mailboxes.AwsSESVerificationState;
import ai.leadplus.domain.mailboxes.MailboxProviderConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailboxProviderConfigDto {

    private String azureId;
    private String googleId;
    private String googleLabelId;
    private String refreshToken;
    private LocalDateTime connectedAt;
    private String smtpAppPassword;
    private AwsSESVerificationState awsSESVerificationState;

    public static MailboxProviderConfigDto fromEntity(MailboxProviderConfig config) {
        if (config == null) {
            return null;
        }
        return MailboxProviderConfigDto.builder()
                .azureId(config.getAzureId())
                .googleId(config.getGoogleId())
                .googleLabelId(config.getGoogleLabelId())
                .refreshToken(config.getRefreshToken())
                .connectedAt(config.getConnectedAt())
                .smtpAppPassword(config.getSmtpAppPassword())
                .awsSESVerificationState(config.getAwsSESVerificationState())
                .build();
    }

    public MailboxProviderConfig toEntity() {
        return MailboxProviderConfig.builder()
                .azureId(azureId)
                .googleId(googleId)
                .googleLabelId(googleLabelId)
                .refreshToken(refreshToken)
                .connectedAt(connectedAt)
                .smtpAppPassword(smtpAppPassword)
                .awsSESVerificationState(awsSESVerificationState)
                .build();
    }
}
