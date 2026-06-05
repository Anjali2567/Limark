package ai.leadplus.api.common;

import ai.leadplus.application.campaigns.CampaignDto;
import ai.leadplus.application.campaigns.CampaignService;
import ai.leadplus.application.exception.UnauthorizedException;
import ai.leadplus.application.tenants.TenantDto;
import ai.leadplus.application.tenants.TenantService;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.workspaces.WorkspaceDto;
import ai.leadplus.application.workspaces.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TenantValidator {

    private final UserValidator userValidator;
    private final TenantService tenantService;
    private final CampaignService campaignService;
    private final WorkspaceService workspaceService;

    public void validateTenantOwner(Long tenantId) {
        TenantDto tenantDto = tenantService.getTenant(tenantId);
        UserDto userDto = userValidator.getAuthenticatedUser();
        if (!Objects.equals(tenantDto.getOwnerId(), userDto.getId())) {
            throw new UnauthorizedException("Insufficient permissions: User is not the owner of the tenant.");
        }
    }

    public Long getAuthenticatedUserIdByTenantId(Long tenantId) {
        TenantDto tenantDto = tenantService.getTenant(tenantId);
        UserDto userDto = userValidator.getAuthenticatedUser();
        if (!Objects.equals(userDto.getTenantId(), tenantDto.getId())) {
            throw new UnauthorizedException("The user is not associated with the provided tenant ID");
        }
        return userDto.getId();
    }

    public void validateAuthenticatedUserByTenantId(Long tenantId) {
        TenantDto tenantDto = tenantService.getTenant(tenantId);
        UserDto userDto = userValidator.getAuthenticatedUser();
        if (!Objects.equals(userDto.getTenantId(), tenantDto.getId())) {
            throw new UnauthorizedException("The user is not associated with the provided tenant ID");
        }
    }

    public void validateCampaignByTenantId(Long tenantId, Long campaignId) {
        TenantDto tenantDto = tenantService.getTenant(tenantId);
        CampaignDto campaignDto = campaignService.getCampaignById(campaignId);
        if (!Objects.equals(campaignDto.getTenantId(), tenantDto.getId())) {
            throw new UnauthorizedException("The campaign does not belong to the specified tenant.");
        }
    }

    public void validateWorkspaceByTenantId(Long tenantId, Long workspaceId) {
        TenantDto tenantDto = tenantService.getTenant(tenantId);
        WorkspaceDto workspaceDto = workspaceService.getWorkspaceById(workspaceId);
        if (!Objects.equals(workspaceDto.getTenantId(), tenantDto.getId())) {
            throw new UnauthorizedException("The workspace does not belong to the specified tenant.");
        }
    }

    public Long getAuthenticatedUserIdByTenantIdAndCampaignId(Long tenantId, Long campaignId) {
        validateCampaignByTenantId(tenantId, campaignId);
        return getAuthenticatedUserIdByTenantId(tenantId);
    }
}
