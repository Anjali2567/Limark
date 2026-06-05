package ai.leadplus.domain.mailboxes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailboxProviderConfig {
    private String azureId;
    private String googleId;
    private String googleLabelId;
    private String refreshToken;
    private LocalDateTime connectedAt;
    private String smtpAppPassword;
    private AwsSESVerificationState awsSESVerificationState;
}
