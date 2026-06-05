package ai.leadplus.api.v1.tenants;

import ai.leadplus.api.v1.mailboxes.MailboxConnectionStatus;
import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.mailboxes.MailboxProviderConfigDto;
import ai.leadplus.application.tenants.TenantDto;
import ai.leadplus.domain.mailboxes.MailBoxType;
import ai.leadplus.domain.tenants.Module;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {

    private Long id;
    private String name;
    private String profileContext;
    private Long ownerId;
    private List<Module> modules;
    private boolean zohoConnected;
    private String zohoEmail;
    private LocalDateTime zohoConnectedAt;
    private boolean hubspotConnected;
    private String hubspotEmail;
    private LocalDateTime hubspotConnectedAt;
    private String announcementFromEmail;
    private String announcementSenderName;
    private MailBoxType announcementType;
    private MailboxConnectionStatus announcementStatus;
    private List<RecipientDto> ccRecipients;
    private List<RecipientDto> bccRecipients;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TenantResponse fromDto(TenantDto tenantDto) {
        return TenantResponse.builder()
                .id(tenantDto.getId())
                .name(tenantDto.getName())
                .profileContext(tenantDto.getProfileContext())
                .ownerId(tenantDto.getOwnerId())
                .modules(tenantDto.getModules())
                .zohoConnected(StringUtils.hasText(tenantDto.getZohoRefreshToken()))
                .zohoEmail(tenantDto.getZohoEmail())
                .zohoConnectedAt(tenantDto.getZohoConnectedAt())
                .hubspotConnected(StringUtils.hasText(tenantDto.getHubspotRefreshToken()))
                .hubspotEmail(tenantDto.getHubspotEmail())
                .hubspotConnectedAt(tenantDto.getHubspotConnectedAt())
                .announcementFromEmail(tenantDto.getAnnouncementFromEmail())
                .announcementSenderName(tenantDto.getAnnouncementSenderName())
                .announcementType(tenantDto.getAnnouncementType())
                .announcementStatus(getAnnouncementStatus(tenantDto))
                .ccRecipients(tenantDto.getCcRecipients())
                .bccRecipients(tenantDto.getBccRecipients())
                .createdAt(tenantDto.getCreatedAt())
                .updatedAt(tenantDto.getUpdatedAt())
                .build();
    }

    private static MailboxConnectionStatus getAnnouncementStatus(TenantDto tenantDto) {
        MailboxProviderConfigDto config = tenantDto.getAnnouncementMetaData();
        if (config == null) return MailboxConnectionStatus.UNVERIFIED;

        if (Objects.equals(tenantDto.getAnnouncementType(), MailBoxType.SMTP)
                && StringUtils.hasText(config.getSmtpAppPassword())) {
            return MailboxConnectionStatus.VERIFIED;
        }

        return MailboxConnectionStatus.UNVERIFIED;
    }
}
