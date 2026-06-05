package ai.leadplus.application.tenants;

import ai.leadplus.application.campaigncontacts.CampaignContactDto;
import ai.leadplus.application.campaigncontacts.CampaignContactService;
import ai.leadplus.application.campaigns.CampaignAnalyticsDto;
import ai.leadplus.application.campaigns.CampaignAnalyticsService;
import ai.leadplus.application.campaigns.CampaignService;
import ai.leadplus.application.contactemails.ContactEmailService;
import ai.leadplus.application.contactoutreachstatuses.ContactOutreachStatusService;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.users.UserService;
import ai.leadplus.domain.campaigns.CampaignStatus;
import ai.leadplus.domain.tenants.Tenant;
import ai.leadplus.domain.tenants.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantAnalyticsService {

    private final TenantRepository tenantRepository;
    private final CampaignService campaignService;
    private final ContactEmailService contactEmailService;
    private final ContactOutreachStatusService contactOutreachStatusService;
    private final UserService userService;
    private final CampaignContactService campaignContactService;
    private final CampaignAnalyticsService campaignAnalyticsService;

    private static final List<CampaignStatus> ACTIVE_STATUSES = List.of(
            CampaignStatus.APPROVED, CampaignStatus.RUNNING, CampaignStatus.PAUSED
    );

    public TenantAnalyticsDto getAnalytics(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId + "."));

        long totalCampaigns = campaignService.countByTenantId(tenantId);
        long activeCampaigns = campaignService.countActiveByTenantId(tenantId, ACTIVE_STATUSES);

        List<Long> campaignIds = campaignService.findIdsByTenantId(tenantId);
        List<CampaignContactDto> contacts = campaignContactService.getCampaignParticipatingContactsByCampaignIds(campaignIds);
        CampaignAnalyticsDto analytics = campaignAnalyticsService.calculateWorkspaceAnalytics(contacts);

        long individualEmails = contactEmailService.countByTenantId(tenantId);
        long workspaceUsers = userService.countActiveByTenantId(tenantId);
        LocalDateTime latestIndividualEmail = contactEmailService.findLatestCreatedAtByTenantId(tenantId);
        LocalDateTime latestCampaignEmail = contactOutreachStatusService.findLatestCampaignEmailAtByTenantId(tenantId);
        LocalDateTime lastActivity = latestIndividualEmail == null ? latestCampaignEmail
                : latestCampaignEmail == null ? latestIndividualEmail
                : latestIndividualEmail.isAfter(latestCampaignEmail) ? latestIndividualEmail : latestCampaignEmail;

        return TenantAnalyticsDto.builder()
                .tenantName(tenant.getName())
                .totalCampaigns(totalCampaigns)
                .activeCampaigns(activeCampaigns)
                .emailsSent(analytics.getSentEmails())
                .individualEmails(individualEmails)
                .workspaceUsers(workspaceUsers)
                .lastActivity(lastActivity)
                .build();
    }

    public TenantAnalyticsDto getAnalytics(String tenantId) {
        return getAnalytics(Long.parseLong(tenantId));
    }
}
