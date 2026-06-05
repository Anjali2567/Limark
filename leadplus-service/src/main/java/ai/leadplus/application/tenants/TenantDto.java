package ai.leadplus.application.tenants;

import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.common.utils.RecipientUtils;
import ai.leadplus.application.mailboxes.MailboxProviderConfigDto;
import ai.leadplus.domain.mailboxes.MailBoxType;
import ai.leadplus.domain.tenants.Module;
import ai.leadplus.domain.tenants.Tenant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantDto {
    private Long id;
    private String name;
    private String domain;
    private String profileContext;
    private Long ownerId;
    private List<Module> modules;
    private String zohoUserId;
    private String zohoEmail;
    private String zohoRefreshToken;
    private LocalDateTime zohoConnectedAt;
    private String hubspotRefreshToken;
    private String hubspotUserId;
    private String hubspotEmail;
    private LocalDateTime hubspotConnectedAt;
    private String announcementFromEmail;
    private String announcementSenderName;
    private MailBoxType announcementType;
    private MailboxProviderConfigDto announcementMetaData;
    private List<RecipientDto> ccRecipients;
    private List<RecipientDto> bccRecipients;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TenantDto fromEntity(Tenant tenant) {
        return TenantDto.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .domain(tenant.getDomain())
                .profileContext(tenant.getProfileContext())
                .ownerId(tenant.getOwnerId())
                .modules(tenant.getModules())
                .zohoUserId(tenant.getZohoUserId())
                .zohoEmail(tenant.getZohoEmail())
                .zohoRefreshToken(tenant.getZohoRefreshToken())
                .zohoConnectedAt(tenant.getZohoConnectedAt())
                .hubspotRefreshToken(tenant.getHubspotRefreshToken())
                .hubspotUserId(tenant.getHubspotUserId())
                .hubspotEmail(tenant.getHubspotEmail())
                .hubspotConnectedAt(tenant.getHubspotConnectedAt())
                .announcementFromEmail(tenant.getAnnouncementFromEmail())
                .announcementSenderName(tenant.getAnnouncementSenderName())
                .announcementType(tenant.getAnnouncementType())
                .announcementMetaData(MailboxProviderConfigDto.fromEntity(tenant.getAnnouncementMetaData()))
                .ccRecipients(RecipientUtils.mapToDto(tenant.getCcRecipients()))
                .bccRecipients(RecipientUtils.mapToDto(tenant.getBccRecipients()))
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }

    public Tenant toEntity() {
        return Tenant.builder()
                .id(id)
                .name(name)
                .domain(domain)
                .profileContext(profileContext)
                .ownerId(ownerId)
                .modules(modules)
                .zohoUserId(zohoUserId)
                .zohoEmail(zohoEmail)
                .zohoRefreshToken(zohoRefreshToken)
                .zohoConnectedAt(zohoConnectedAt)
                .hubspotRefreshToken(hubspotRefreshToken)
                .hubspotUserId(hubspotUserId)
                .hubspotEmail(hubspotEmail)
                .hubspotConnectedAt(hubspotConnectedAt)
                .announcementFromEmail(announcementFromEmail)
                .announcementSenderName(announcementSenderName)
                .announcementType(announcementType)
                .announcementMetaData(announcementMetaData != null ? announcementMetaData.toEntity() : null)
                .ccRecipients(RecipientUtils.mapToEntity(ccRecipients))
                .bccRecipients(RecipientUtils.mapToEntity(bccRecipients))
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
