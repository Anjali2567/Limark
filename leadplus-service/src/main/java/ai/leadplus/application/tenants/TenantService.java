package ai.leadplus.application.tenants;

import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.common.utils.StringNormalizer;
import ai.leadplus.application.common.utils.UrlUtils;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.hubspot.HubSpotCompanySyncService;
import ai.leadplus.application.exception.SmtpConnectionException;
import ai.leadplus.application.hubspot.HubSpotContactSyncService;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.application.vendors.VendorApprovedEvent;
import ai.leadplus.application.workspaces.WorkspaceDto;
import ai.leadplus.application.workspaces.WorkspaceService;
import ai.leadplus.application.workspaceuser.WorkspaceUserDto;
import ai.leadplus.application.workspaceuser.WorkspaceUserService;
import ai.leadplus.application.zoho.ZohoCompanySyncService;
import ai.leadplus.application.zoho.ZohoContactSyncService;
import ai.leadplus.domain.mailboxes.MailBoxType;
import ai.leadplus.domain.mailboxes.MailboxProviderConfig;
import ai.leadplus.domain.tenants.Module;
import ai.leadplus.domain.tenants.Tenant;
import ai.leadplus.domain.tenants.TenantRepository;
import ai.leadplus.domain.users.UserRole;
import ai.leadplus.infrastructure.hubspot.auth.HubSpotAccessTokenResponse;
import ai.leadplus.infrastructure.hubspot.auth.HubSpotAuthClient;
import ai.leadplus.infrastructure.hubspot.user.HubSpotUserClient;
import ai.leadplus.infrastructure.hubspot.user.HubSpotUserResponse;
import ai.leadplus.infrastructure.springmail.SpringMailClient;
import ai.leadplus.infrastructure.springmail.SpringMailRequest;
import ai.leadplus.infrastructure.zoho.auth.ZohoAuthClient;
import ai.leadplus.infrastructure.zoho.auth.ZohoRefreshTokenResponse;
import ai.leadplus.infrastructure.zoho.user.ZohoUserClient;
import ai.leadplus.infrastructure.zoho.user.ZohoUserResponse;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantSearchService tenantSearchService;
    private final ZohoAuthClient zohoAuthClient;
    private final ZohoUserClient zohoUserClient;
    private final ZohoCompanySyncService zohoCompanySyncService;
    private final ZohoContactSyncService zohoContactSyncService;
    private final HubSpotAuthClient hubSpotAuthClient;
    private final HubSpotUserClient hubSpotUserClient;
    private final HubSpotCompanySyncService hubSpotCompanySyncService;
    private final HubSpotContactSyncService hubSpotContactSyncService;
    private final WorkspaceService workspaceService;
    private final UserService userService;
    private final WorkspaceUserService workspaceUserService;
    private final SpringMailClient springMailClient;

    private static final List<Module> VENDOR_APPROVAL_MODULES = List.of(Module.VENDOR, Module.LEAD_GENERATION);

    public TenantDto createTenant(String name) {
        TenantDto tenantDto = TenantDto.builder()
                .name(name)
                .build();
        Tenant tenant = tenantRepository.save(tenantDto.toEntity());
        return TenantDto.fromEntity(tenant);
    }

    public List<Module> getEnabledModules(Long tenantId) {
        Tenant tenant = findTenantById(tenantId);
        return tenant.getModules();
    }

    public List<Module> getEnabledModulesFromOrigin(String origin) {
        String domain = UrlUtils.extractRootDomain(origin);
        if (!StringUtils.hasText(domain)) {
            return Collections.emptyList();
        }
        if (domain.contains("localhost") || domain.contains("leadplus.ai")
                || domain.contains("cloudfront.net")) {
            return Arrays.asList(Module.values());
        }
        return tenantRepository.findByDomain(domain)
                .map(Tenant::getModules)
                .orElse(Collections.emptyList());
    }

    public void updateTenantOwner(Long tenantId, Long ownerId) {
        Tenant tenant = findTenantById(tenantId);
        tenant.setOwnerId(ownerId);
        tenantRepository.save(tenant);
    }

    public TenantDto getTenant(Long id) {
        Tenant tenant = findTenantById(id);
        return TenantDto.fromEntity(tenant);
    }

    public TenantDto getTenant(String id) {
        return getTenant(Long.parseLong(id));
    }

    private Tenant findTenantById(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId + "."));
    }

    public TenantDto connectZohoCRM(Long tenantId, String zohoAuthCode, String redirectUri) {
        Tenant tenant = findTenantById(tenantId);
        ZohoRefreshTokenResponse refreshTokenResponse = zohoAuthClient.getTokenFromCode(zohoAuthCode, redirectUri);
        ZohoUserResponse userResponse = zohoUserClient.getUserInfo(refreshTokenResponse.getRefreshToken());
        tenant.setZohoUserId(userResponse.getZuid());
        tenant.setZohoEmail(userResponse.getEmail());
        tenant.setZohoRefreshToken(refreshTokenResponse.getRefreshToken());
        tenant.setZohoConnectedAt(LocalDateTime.now());
        return TenantDto.fromEntity(tenantRepository.save(tenant));
    }

    public TenantDto disconnectZohoCRM(Long tenantId) {
        Tenant tenant = findTenantById(tenantId);
        tenant.setZohoUserId(null);
        tenant.setZohoEmail(null);
        tenant.setZohoRefreshToken(null);
        tenant.setZohoConnectedAt(null);
        return TenantDto.fromEntity(tenantRepository.save(tenant));
    }

    public void syncZohoRecords(Long tenantId) {
        Tenant tenant = findTenantById(tenantId);
        if (!StringUtils.hasText(tenant.getZohoRefreshToken())) {
            throw new BadRequestException("Zoho CRM is not connected for this tenant");
        }
        zohoCompanySyncService.syncCompanies(tenant.getZohoRefreshToken(), tenant.getId());
        zohoContactSyncService.syncContacts(tenant.getZohoRefreshToken(), tenant.getId());
    }

    public TenantDto connectHubSpotCRM(Long tenantId, String code, String redirectUri) {

        Tenant tenant = findTenantById(tenantId);
        HubSpotAccessTokenResponse token = hubSpotAuthClient.getTokenFromCode(code, redirectUri);
        HubSpotUserResponse user = hubSpotUserClient.getUserInfo(token.getAccessToken());

        tenant.setHubspotUserId(user.getUserId());
        tenant.setHubspotEmail(user.getEmail());
        tenant.setHubspotRefreshToken(token.getRefreshToken());
        tenant.setHubspotConnectedAt(LocalDateTime.now());

        return TenantDto.fromEntity(tenantRepository.save(tenant));
    }

    public TenantDto disconnectHubSpotCRM(Long tenantId) {

        Tenant tenant = findTenantById(tenantId);
        tenant.setHubspotRefreshToken(null);
        tenant.setHubspotUserId(null);
        tenant.setHubspotEmail(null);
        tenant.setHubspotConnectedAt(null);

        return TenantDto.fromEntity(tenantRepository.save(tenant));
    }

    public void syncHubSpotRecords(Long tenantId) {
        Tenant tenant = findTenantById(tenantId);
        if (!StringUtils.hasText(tenant.getHubspotRefreshToken())) {
            throw new BadRequestException("HubSpot CRM is not connected for this tenant");
        }

        hubSpotCompanySyncService.syncCompanies(tenant.getHubspotRefreshToken(), tenant.getId());
        hubSpotContactSyncService.syncContacts(tenant.getHubspotRefreshToken(), tenant.getId());
    }

    public Page<TenantWorkspaceDto> getAllWorkspaces(Long tenantId, String query, Pageable pageable) {
        Page<WorkspaceDto> workspacePage = workspaceService.getAllWorkspacesByTenantId(tenantId, query, pageable);

        Set<Long> ownerIds = workspacePage.getContent().stream()
                .map(WorkspaceDto::getOwnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Long> workspaceIds = workspacePage.getContent().stream()
                .filter(Objects::nonNull)
                .map(WorkspaceDto::getId)
                .distinct()
                .toList();

        Map<Long, UserDto> userMap = userService.getUsersByIds(ownerIds).stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));

        Map<Long, Long> workspaceMemberCountMap =
                workspaceUserService.getAllWorkspaceUsersByWorkspaceIds(workspaceIds)
                        .stream()
                        .filter(WorkspaceUserDto::isActive)
                        .collect(Collectors.groupingBy(
                                WorkspaceUserDto::getWorkspaceId,
                                Collectors.counting()
                        ));

        return workspacePage.map(workspace -> {
            UserDto owner = userMap.get(workspace.getOwnerId());
            return TenantWorkspaceDto.builder()
                    .id(workspace.getId())
                    .workspaceName(workspace.getName())
                    .ownerId(workspace.getOwnerId())
                    .ownerName(owner != null ? owner.getName() : null)
                    .totalMembers(workspaceMemberCountMap.getOrDefault(workspace.getId(), 0L))
                    .createdAt(workspace.getCreatedAt())
                    .build();
        });
    }

    public Page<TenantUserDto> getAllUsers(Long tenantId, String query, Pageable pageable) {

        Page<UserDto> userDtoPage = userService.searchUsers(String.valueOf(tenantId), query, null, null, pageable);

        Set<Long> userIds = userDtoPage
                .stream()
                .map(UserDto::getId)
                .collect(Collectors.toSet());

        List<WorkspaceUserDto> workspaceUserDtoList = workspaceUserService.getAllWorkspaceUsersByUserIds(userIds);

        Map<Long, Long> workspaceCountByUserId = workspaceUserDtoList.stream()
                .collect(Collectors.groupingBy(
                        WorkspaceUserDto::getUserId,
                        Collectors.counting()
                ));

        return userDtoPage.map(userDto ->
                TenantUserDto.builder()
                        .id(userDto.getId())
                        .name(userDto.getName())
                        .email(userDto.getEmail())
                        .workspacesCount(workspaceCountByUserId.getOrDefault(userDto.getId(), 0L))
                        .createdAt(userDto.getCreatedAt())
                        .build()
        );
    }

    public UserDto tenantWorkspaceCreation(UserDto userDto) {
        TenantDto tenantDto;
        String ownerName = StringNormalizer.normalizeEachWord(userDto.getName());
        if (userDto.getTenantId() != null) {
            tenantDto = getTenant(userDto.getTenantId());
        } else {
            String domain = userDto.getEmail().split("@")[1].trim().toLowerCase();
            Optional<Tenant> optionalTenant = tenantRepository.findByDomain(domain);
            if (optionalTenant.isPresent()) {
                tenantDto = TenantDto.fromEntity(optionalTenant.get());
            } else {
                Tenant tenant = Tenant.builder()
                        .name(ownerName + "'s tenant")
                        .domain(domain)
                        .ownerId(userDto.getId())
                        .modules(List.of(Module.CUSTOMER))
                        .build();
                tenantDto = TenantDto.fromEntity(tenantRepository.save(tenant));
                userDto.getRoles().add(UserRole.TENANT_OWNER);
            }
            userDto.setTenantId(tenantDto.getId());
        }
        WorkspaceDto workspaceDto = workspaceService.createWorkspace(
                WorkspaceDto.builder()
                        .name(ownerName + "'s workspace")
                        .tenantId(tenantDto.getId())
                        .ownerId(userDto.getId())
                        .build()
        );
        userDto.setWorkspaceId(workspaceDto.getId());
        userDto = userService.saveUser(userDto);
        return userDto;
    }

    public TenantDto updateAnnouncementConfig(Long tenantId, TenantAnnouncementConfigDto configDto) {
        Tenant tenant = findTenantById(tenantId);

        try {
            InternetAddress internetAddress = new InternetAddress(configDto.getFromEmail(), configDto.getSenderName());
            SpringMailRequest testMail = SpringMailRequest.builder()
                    .subject("SMTP Connection Test")
                    .body("Your SMTP connection has been successfully established.")
                    .fromAddress(internetAddress)
                    .toAddresses(new InternetAddress[]{internetAddress})
                    .build();

            springMailClient.sendEmail(testMail, configDto.getFromEmail(), configDto.getSmtpAppPassword());
        } catch (Exception ex) {
            log.error("Failed to send SMTP test email to {}: {}", configDto.getFromEmail(), ex.getMessage(), ex);
            throw new SmtpConnectionException("Failed to verify SMTP connection: " + ex.getMessage());
        }

        tenant.setAnnouncementFromEmail(configDto.getFromEmail());
        tenant.setAnnouncementSenderName(configDto.getSenderName());
        tenant.setAnnouncementType(MailBoxType.SMTP);
        tenant.setAnnouncementMetaData(MailboxProviderConfig.builder()
                .smtpAppPassword(configDto.getSmtpAppPassword())
                .connectedAt(LocalDateTime.now())
                .build());
        return TenantDto.fromEntity(tenantRepository.save(tenant));
    }

    public TenantDto updateTenantRecipients(Long tenantId, TenantRecipientsDto tenantRecipientsDto) {
        Tenant tenant = findTenantById(tenantId);
        tenant.setCcRecipients(tenantRecipientsDto.getCcRecipients().stream().map(RecipientDto::toEntity).toList());
        tenant.setBccRecipients(tenantRecipientsDto.getBccRecipients().stream().map(RecipientDto::toEntity).toList());
        return TenantDto.fromEntity(tenantRepository.save(tenant));
    }

    public TenantDto updateTenantProfileContext(Long tenantId, String profileContext) {
        Tenant tenant = findTenantById(tenantId);
        tenant.setProfileContext(StringUtils.hasText(profileContext) ? profileContext.trim() : null);
        return TenantDto.fromEntity(tenantRepository.save(tenant));
    }

    public Page<TenantListingDto> getAllTenants(String search, Pageable pageable) {
        Page<Tenant> tenantPage = tenantSearchService.searchTenants(search, pageable);

        Set<Long> ownerIds = tenantPage.stream()
                .map(Tenant::getOwnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, UserDto> userMap = new HashMap<>();
        if (!ownerIds.isEmpty()) {
            userService.getUsersByIds(new ArrayList<>(ownerIds))
                    .forEach(user -> userMap.put(user.getId(), user));
        }

        return tenantPage.map(tenant -> {
            String ownerName = null;
            if (tenant.getOwnerId() != null) {
                UserDto ownerUser = userMap.get(tenant.getOwnerId());
                ownerName = ownerUser != null ? ownerUser.getName() : null;
            }
            return TenantListingDto.fromEntity(tenant, ownerName);
        });
    }

    @EventListener
    public void handleVendorApprovedEvent(VendorApprovedEvent event) {
        Long tenantId = event.getVendor().getTenantId();
        Tenant tenant = findTenantById(tenantId);
        List<Module> moduleList = tenant.getModules();
        if (CollectionUtils.isEmpty(moduleList)) {
            moduleList = new ArrayList<>();
        }
        moduleList.addAll(VENDOR_APPROVAL_MODULES);
        moduleList = moduleList.stream().distinct().toList();
        tenant.setModules(moduleList);
        tenantRepository.save(tenant);
    }
}
