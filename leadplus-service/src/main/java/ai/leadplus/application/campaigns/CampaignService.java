package ai.leadplus.application.campaigns;

import ai.leadplus.application.campaignagent.tools.AgentLeadCountAggregationService;
import ai.leadplus.application.campaignagent.tools.AgentLeadCountResult;
import ai.leadplus.application.campaignchatmemory.CampaignChatMemoryDto;
import ai.leadplus.application.campaignchatmemory.CampaignChatMemoryService;
import ai.leadplus.application.campaigncontacts.CampaignContactDto;
import ai.leadplus.application.campaigncontacts.CampaignContactInfoDto;
import ai.leadplus.application.campaigncontacts.CampaignContactService;
import ai.leadplus.application.campaignemails.CampaignEmailService;
import ai.leadplus.application.common.TargetingCriteriaDto;
import ai.leadplus.application.common.utils.RecipientUtils;
import ai.leadplus.application.contactoutreachstatuses.ContactOutreachStatusService;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.leads.LeadFilterCriteria;
import ai.leadplus.application.leadcompany.LeadCompanyDto;
import ai.leadplus.application.leadcompany.LeadCompanyService;
import ai.leadplus.application.leaddatapacks.GatedInfo;
import ai.leadplus.application.leaddatapacks.LeadDataPackService;
import ai.leadplus.application.mailboxes.MailboxDto;
import ai.leadplus.application.mailboxes.MailboxService;
import ai.leadplus.application.vendordatapacks.VendorAccess;
import ai.leadplus.application.vendordatapacks.VendorDataPackService;
import ai.leadplus.domain.campaigns.Campaign;
import ai.leadplus.domain.campaigns.CampaignIdDto;
import ai.leadplus.domain.campaigns.CampaignRepository;
import ai.leadplus.domain.campaigns.CampaignStatus;
import ai.leadplus.domain.mailboxes.MailBoxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CampaignContactService campaignContactService;
    private final LeadCompanyService leadCompanyService;
    private final MailboxService mailboxService;
    private final CampaignAnalyticsService campaignAnalyticsService;
    private final CampaignChatMemoryService campaignChatMemoryService;
    private final CampaignEmailService campaignEmailService;
    private final AgentLeadCountAggregationService agentLeadCountAggregationService;
    private final LeadDataPackService leadDataPackService;
    private final VendorDataPackService vendorDataPackService;
    private final ContactOutreachStatusService contactOutreachStatusService;

    private static final List<MailBoxType> SUPPORTED_MAILBOX_TYPES = List.of(
            MailBoxType.OUTLOOK,
            MailBoxType.GMAIL,
            MailBoxType.SMTP,
            MailBoxType.SES
    );

    public CampaignDto createBasicCampaign(Long tenantId, Long workspaceId) {
        Campaign campaign = Campaign.builder()
                .tenantId(tenantId)
                .workspaceId(workspaceId)
                .status(CampaignStatus.DRAFT)
                .build();
        campaignRepository.save(campaign);
        return CampaignDto.fromEntity(campaign);
    }

    public CampaignDto createCampaignForSearch(Long tenantId,
                                               Long workspaceId,
                                               List<Long> contactIds,
                                               LeadFilterCriteria leadFilterCriteria) {
        if (contactIds.isEmpty()) {
            throw new BadRequestException("No contacts found for campaign creation.");
        }
        Campaign campaign = Campaign.builder()
                .tenantId(tenantId)
                .workspaceId(workspaceId)
                .name("Campaign from Lead Search")
                .status(CampaignStatus.DRAFT)
                .leadFilter(leadFilterCriteria)
                .build();
        campaignRepository.save(campaign);
        Set<Long> uniqueContactIds = new HashSet<>(contactIds);
        campaignContactService.updateContactIdsOfCampaign(campaign.getTenantId(), campaign.getId(), uniqueContactIds, true);
        campaignEmailService.createDefaultEmailSequence(campaign.getId());
        return CampaignDto.fromEntity(campaign);
    }

    public CampaignDto getCampaignById(Long id) {
        return CampaignDto.fromEntity(findCampaignById(id));
    }

    public CampaignInfoDto getCampaignDataById(Long tenantId, Long id) {
        Campaign campaign = findCampaignById(id);

        List<CampaignContactInfoDto> campaignContactInfoDtoList =
                campaignContactService.getCampaignContactsByCampaignIds(tenantId, List.of(campaign.getId()), false);
        Map<Long, CampaignTotalDto> campaignTotals = getTotalCompaniesAndContactsForCampaigns(campaignContactInfoDtoList);
        CampaignTotalDto campaignTotalDto = campaignTotals.getOrDefault(id,
                CampaignTotalDto.builder()
                        .totalCompanies(0)
                        .totalContacts(0)
                        .campaignCompanies(List.of())
                        .build());
        return CampaignInfoDto.fromEntity(
                campaign,
                campaignTotalDto.getTotalCompanies(),
                campaignTotalDto.getTotalContacts(),
                campaignTotalDto.getCampaignCompanies());
    }

    public CampaignInfoDto getCampaignFromChatMemoryId(Long tenantId, Long campaignChatMemoryId) {
        CampaignChatMemoryDto campaignChatMemoryDto =
                campaignChatMemoryService.getCampaignChatMemoryById(campaignChatMemoryId);
        TargetingCriteriaDto targetingCriteriaDto = campaignChatMemoryDto.getTargetingCriteria();

        if (campaignChatMemoryDto.getLeadFilter() == null) {
            long storedCompanies = targetingCriteriaDto != null ? targetingCriteriaDto.getTotalCompanies() : 0;
            long storedContacts  = targetingCriteriaDto != null ? targetingCriteriaDto.getTotalContacts()  : 0;

            if (campaignChatMemoryDto.getContactLimit() != null && campaignChatMemoryDto.getContactLimit() > 0) {
                storedContacts = Math.min(storedContacts, campaignChatMemoryDto.getContactLimit());
            }

            return CampaignInfoDto.fromChatMemory(
                    campaignChatMemoryDto,
                    targetingCriteriaDto,
                    storedCompanies,
                    storedContacts,
                    List.of()
            );
        }

        GatedInfo gatedInfo = leadDataPackService.getGatedInfo();
        VendorAccess vendorAccess = vendorDataPackService
                .getVendorAccessForAuthenticatedUser(tenantId)
                .orElse(null);

        AgentLeadCountResult countResult = agentLeadCountAggregationService.count(
                campaignChatMemoryDto.getLeadFilter(),
                tenantId,
                gatedInfo,
                vendorAccess,
                contactOutreachStatusService.getIneligibleContactIds(tenantId)
        );

        long effectiveTotalContacts = countResult.getTotalContacts();
        if (campaignChatMemoryDto.getContactLimit() != null
                && campaignChatMemoryDto.getContactLimit() > 0) {
            effectiveTotalContacts = Math.min(effectiveTotalContacts,
                    campaignChatMemoryDto.getContactLimit());
        }

        return CampaignInfoDto.fromChatMemory(
                campaignChatMemoryDto,
                targetingCriteriaDto,
                countResult.getTotalCompanies(),
                effectiveTotalContacts,
                countResult.getSampleCompanyNames().stream()
                        .map(name -> CampaignCompanyDto.builder().name(name).build())
                        .toList()
        );
    }

    public CampaignDto findCampaignByConversationId(Long workspaceId, String conversationId) {
        Campaign campaign = campaignRepository
                .findByWorkspaceIdAndTargetingCriteria_ConversationId(workspaceId, conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Campaign not found with conversationId: " + conversationId));
        return CampaignDto.fromEntity(campaign);
    }

    public Map<Long, CampaignDto> getCampaignsByIds(Set<Long> campaignIds) {
        if (CollectionUtils.isEmpty(campaignIds)) return Collections.emptyMap();
        List<Campaign> campaigns = campaignRepository.findAllById(campaignIds);
        return campaigns.stream()
                .map(CampaignDto::fromEntity)
                .collect(Collectors.toMap(CampaignDto::getId, Function.identity()));
    }

    public long countByTenantId(Long tenantId) {
        return campaignRepository.countByTenantId(tenantId);
    }

    public long countActiveByTenantId(Long tenantId, List<CampaignStatus> activeStatuses) {
        return campaignRepository.countByTenantIdAndStatusIn(tenantId, activeStatuses);
    }

    public List<Long> findIdsByTenantId(Long tenantId) {
        return campaignRepository.findIdsByTenantId(tenantId).stream()
                .map(CampaignIdDto::getId)
                .toList();
    }

    public CampaignDto updateCampaign(CampaignDto campaignDto) {
        Campaign campaign = campaignDto.toEntity();
        campaignRepository.save(campaign);
        return CampaignDto.fromEntity(campaign);
    }

    public CampaignDto updateCampaignDetails(Long id, CampaignRequestDto campaignRequestDto) {
        Campaign campaign = findCampaignById(id);
        campaign.setName(campaignRequestDto.getName());
        campaign.setSendingWindow(campaignRequestDto.getSendingWindow());
        campaign = campaignRepository.save(campaign);
        return CampaignDto.fromEntity(campaign);
    }

    public CampaignDto updateCampaignRecipients(Long id, CampaignDto campaignDto) {
        Campaign campaign = findCampaignById(id);
        campaign.setCcRecipients(RecipientUtils.mapToEntity(campaignDto.getCcRecipients()));
        campaign.setBccRecipients(RecipientUtils.mapToEntity(campaignDto.getBccRecipients()));
        campaign = campaignRepository.save(campaign);
        return CampaignDto.fromEntity(campaign);
    }

    public CampaignDto updateCampaignSendingMailbox(Long id, Long mailboxId, Long workspaceId, Long userId) {
        Campaign campaign = findCampaignById(id);
        MailboxDto mailboxDto = mailboxService.getAccessibleMailboxById(mailboxId, workspaceId, userId);
        campaign.setSendingMailboxId(mailboxDto.getId());
        campaign = campaignRepository.save(campaign);
        return CampaignDto.fromEntity(campaign);
    }

    public void launchCampaignById(Long userId, Long workspaceId, Long campaignId) {
        Campaign campaign = findCampaignById(campaignId);

        if (Objects.equals(campaign.getStatus(), CampaignStatus.RUNNING)) {
            return;
        }

        if (Objects.equals(campaign.getStatus(), CampaignStatus.PENDING_APPROVAL)) {
            throw new BadRequestException(
                    "Campaign contacts are still being prepared. Please wait a moment and try again.");
        }

        if (!Objects.equals(campaign.getStatus(), CampaignStatus.DRAFT)) {
            throw new BadRequestException("Only draft campaigns can be launched.");
        }

        if (campaign.getSendingMailboxId() == null) {
            throw new BadRequestException("Select a sending mailbox before launching the campaign.");
        }

        MailboxDto mailboxDto = mailboxService.getAccessibleMailboxById(campaign.getSendingMailboxId(), workspaceId, userId);
        if (!SUPPORTED_MAILBOX_TYPES.contains(mailboxDto.getType())) {
            throw new BadRequestException(
                    "The selected mailbox type is not supported for sending campaigns. Please select a different mailbox.");
        }

        campaign.setStatus(CampaignStatus.RUNNING);
        campaign.setLaunchedAt(LocalDateTime.now());
        campaignRepository.save(campaign);
        eventPublisher.publishEvent(new CampaignLaunchedEvent(this, CampaignDto.fromEntity(campaign)));
    }

    public void pauseCampaignById(Long campaignId) {
        Campaign campaign = findCampaignById(campaignId);
        if (Objects.equals(campaign.getStatus(), CampaignStatus.PAUSED)) return;
        if (!Objects.equals(campaign.getStatus(), CampaignStatus.RUNNING)) {
            throw new BadRequestException("Only running campaigns can be paused.");
        }
        campaign.setStatus(CampaignStatus.PAUSED);
        campaignRepository.save(campaign);
        eventPublisher.publishEvent(new CampaignPausedEvent(this, CampaignDto.fromEntity(campaign)));
    }

    public void resumeCampaignById(Long campaignId) {
        Campaign campaign = findCampaignById(campaignId);
        if (Objects.equals(campaign.getStatus(), CampaignStatus.RUNNING)) return;
        if (!Objects.equals(campaign.getStatus(), CampaignStatus.PAUSED)) {
            throw new BadRequestException("Only paused campaigns can be resumed.");
        }
        campaign.setStatus(CampaignStatus.RUNNING);
        campaignRepository.save(campaign);
        eventPublisher.publishEvent(new CampaignResumeEvent(this, CampaignDto.fromEntity(campaign)));
    }

    public void markCampaignAsCompleted(Long campaignId) {
        Campaign campaign = findCampaignById(campaignId);
        campaign.setStatus(CampaignStatus.COMPLETED);
        campaignRepository.save(campaign);
        eventPublisher.publishEvent(new CampaignCompletedEvent(this, CampaignDto.fromEntity(campaign)));
    }

    public void deleteCampaignById(Long id) {
        Campaign campaign = findCampaignById(id);
        eventPublisher.publishEvent(new CampaignDeletedEvent(this, CampaignDto.fromEntity(campaign)));
        campaignRepository.delete(campaign);
    }

    public CampaignDto proceedCampaignFromChatMemory(Long campaignChatMemoryId) {
        CampaignChatMemoryDto campaignChatMemory =
                campaignChatMemoryService.getCampaignChatMemoryById(campaignChatMemoryId);
        TargetingCriteriaDto targetingCriteriaDto = campaignChatMemory.getTargetingCriteria();

        Campaign campaign = Campaign.builder()
                .tenantId(campaignChatMemory.getTenantId())
                .workspaceId(campaignChatMemory.getWorkspaceId())
                .name(campaignChatMemory.getName())
                .industry(campaignChatMemory.getIndustry())
                .status(CampaignStatus.DRAFT)
                .targetingCriteria(targetingCriteriaDto != null ? targetingCriteriaDto.toEntity() : null)
                .build();
        campaignRepository.save(campaign);

        campaignChatMemory.setCampaignId(campaign.getId());
        campaignChatMemoryService.updateCampaignChatMemory(campaignChatMemory);

        GatedInfo gatedInfo = leadDataPackService.getGatedInfo();
        VendorAccess vendorAccess = vendorDataPackService
                .getVendorAccessForAuthenticatedUser(campaign.getTenantId())
                .orElse(null);

        eventPublisher.publishEvent(new CampaignProceededEvent(
                this,
                CampaignDto.fromEntity(campaign),
                campaignChatMemory.getLeadFilter(),
                campaignChatMemory.getContactLimit(),
                gatedInfo,
                vendorAccess
        ));

        campaignEmailService.createDefaultEmailSequence(campaign.getId());
        return CampaignDto.fromEntity(campaign);
    }

    public CampaignAnalyticsDto getCampaignAnalyticsByCampaignId(Long campaignId) {
        List<CampaignContactDto> contacts =
                campaignContactService.getCampaignParticipatingContactsByCampaignIds(List.of(campaignId));
        return campaignAnalyticsService.calculateWorkspaceAnalytics(contacts);
    }

    public CampaignAnalyticsDto getFullCampaignAnalytics(Long workspaceId) {
        List<Campaign> campaigns = campaignRepository.findAllByWorkspaceId(workspaceId);
        List<Long> campaignIds = campaigns.stream().map(Campaign::getId).toList();
        List<CampaignContactDto> contacts =
                campaignContactService.getCampaignParticipatingContactsByCampaignIds(campaignIds);
        return campaignAnalyticsService.calculateWorkspaceAnalytics(contacts);
    }

    public Map<Long, CampaignTotalDto> getTotalCompaniesAndContactsForCampaigns(
            List<CampaignContactInfoDto> campaignContactInfoDtoList) {

        if (CollectionUtils.isEmpty(campaignContactInfoDtoList)) return Map.of();

        Map<Long, Map<Long, List<CampaignContactInfoDto>>> campaignToCompanyMap =
                campaignContactInfoDtoList.stream()
                        .collect(Collectors.groupingBy(
                                CampaignContactInfoDto::getCampaignId,
                                Collectors.groupingBy(CampaignContactInfoDto::getLeadCompanyId)));

        List<Long> allCompanyIds = campaignContactInfoDtoList.stream()
                .map(CampaignContactInfoDto::getLeadCompanyId)
                .distinct()
                .toList();

        List<LeadCompanyDto> leadCompanyDtoList = leadCompanyService.getLeadCompaniesByIds(allCompanyIds);
        Map<Long, LeadCompanyDto> companyIdToDtoMap = leadCompanyDtoList.stream()
                .collect(Collectors.toMap(LeadCompanyDto::getId, c -> c));

        return campaignToCompanyMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            Map<Long, List<CampaignContactInfoDto>> companyMap = entry.getValue();
                            List<CampaignCompanyDto> campaignCompanies = companyMap.entrySet().stream()
                                    .map(companyEntry -> {
                                        LeadCompanyDto companyDto = companyIdToDtoMap.get(companyEntry.getKey());
                                        List<CampaignContactInfoDto> contacts = companyEntry.getValue();
                                        return CampaignCompanyDto.fromEntity(companyDto, contacts);
                                    })
                                    .filter(Objects::nonNull)
                                    .toList();

                            long totalCompanies = campaignCompanies.size();
                            long totalContacts  = companyMap.values().stream().mapToLong(List::size).sum();
                            return CampaignTotalDto.builder()
                                    .totalCompanies(totalCompanies)
                                    .totalContacts(totalContacts)
                                    .campaignCompanies(campaignCompanies)
                                    .build();
                        }));
    }

    private Campaign findCampaignById(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
    }
}