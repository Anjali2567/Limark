package ai.leadplus.api.v1.mailboxes;

import ai.leadplus.application.mailboxes.MailboxDto;
import ai.leadplus.application.mailboxes.MailboxProviderConfigDto;
import ai.leadplus.domain.mailboxes.AwsSESVerificationState;
import ai.leadplus.domain.mailboxes.MailBoxType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailboxResponse {

    private String id;
    private Long userId;
    private Long workspaceId;
    private String emailAddress;
    private MailBoxType type;
    private MailboxConnectionStatus status;
    private boolean tokenExpired;

    public static MailboxResponse fromDto(MailboxDto mailboxDto) {
        return MailboxResponse.builder()
                .id(String.valueOf(mailboxDto.getId()))
                .userId(mailboxDto.getUserId())
                .workspaceId(mailboxDto.getWorkspaceId())
                .emailAddress(mailboxDto.getEmailAddress())
                .type(mailboxDto.getType())
                .status(getStatus(mailboxDto))
                .tokenExpired(mailboxDto.isTokenExpired())
                .build();
    }

    private static MailboxConnectionStatus getStatus(MailboxDto mailboxDto) {
        MailboxProviderConfigDto configDto = mailboxDto.getMetaData();
        if (configDto == null) return MailboxConnectionStatus.UNVERIFIED;

        if (Objects.equals(mailboxDto.getType(), MailBoxType.OUTLOOK) && StringUtils.hasText(configDto.getAzureId())) {
            return MailboxConnectionStatus.VERIFIED;
        }

        if (Objects.equals(mailboxDto.getType(), MailBoxType.GMAIL) && StringUtils.hasText(configDto.getGoogleId())) {
            return MailboxConnectionStatus.VERIFIED;
        }

        if (Objects.equals(mailboxDto.getType(), MailBoxType.SES)) {
            AwsSESVerificationState verificationState = configDto.getAwsSESVerificationState();
            if (verificationState == null) return MailboxConnectionStatus.UNVERIFIED;

            return switch (verificationState) {
                case VERIFIED ->  MailboxConnectionStatus.VERIFIED;
                case PENDING ->  MailboxConnectionStatus.PENDING;
                default -> MailboxConnectionStatus.UNVERIFIED;
            };
        }
        return MailboxConnectionStatus.UNVERIFIED;
    }
}
