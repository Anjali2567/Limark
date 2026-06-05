package ai.leadplus.application.campaigncontacts;

import ai.leadplus.application.campaigns.*;
import ai.leadplus.application.contactoutreachstatuses.ContactOutreachStatusService;
import ai.leadplus.application.contactoutreachstatuses.ContactUnsubscribedEvent;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.application.leadcontact.LeadContactService;
import ai.leadplus.application.tenantcontacts.TenantContactService;
import ai.leadplus.domain.campaigncontacts.CampaignContact;
import ai.leadplus.domain.tenantcontactmetadata.TenantContactMetadata;
import ai.leadplus.domain.tenantcontactmetadata.TenantContactMetadataRepository;
import ai.leadplus.domain.campaigncontacts.CampaignContactRepository;
import ai.leadplus.domain.campaigncontacts.CampaignContactStatus;
import ai.leadplus.domain.campaigns.Campaign;
import ai.leadplus.domain.campaigns.CampaignRepository;
import ai.leadplus.domain.campaigns.CampaignStatus;
import ai.leadplus.domain.mailboxes.MailBoxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignContactService {

    private final CampaignContactRepository campaignContactRepository;
    private final CampaignRepository campaignRepository;
    private final LeadContactService leadContactService;
    private final ContactOutreachStatusService contactOutreachStatusService;
    private final TenantContactService tenantContactService;
    private final TenantContactMetadataRepository tenantContactMetadataRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final List<CampaignStatus> ALLOWED_ADD_STATUSES = List.of(
            CampaignStatus.PAUSED, CampaignStatus.DRAFT, CampaignStatus.PENDING_APPROVAL
    );

    public Optional<CampaignContactInfoDto> getTopCampaignContactToMail() {
        Optional<CampaignContact> campaignContactDto = campaignContactRepository.findTopByStatusAndNextSendAtBeforeAndParticipatingTrueOrderByUpdatedAtAsc(
                CampaignContactStatus.ACTIVE,
                LocalDateTime.now()
        );
        return campaignContactDto.map(this::mapToCampaignContactInfoDto);
    }

    public List<CampaignContactDto> getCampaignParticipatingContactsByCampaignIds(List<Long> campaignIds) {
        if (CollectionUtils.isEmpty(campaignIds)) return List.of();
        return campaignContactRepository.findAllByCampaignIdInAndParticipatingTrue(campaignIds).stream().map(CampaignContactDto::fromEntity).collect(Collectors.toList());
    }

    public List<CampaignContactInfoDto> getCampaignContactsByCampaignIds(Long tenantId, List<Long> campaignIds, boolean isParticipating) {
        if (CollectionUtils.isEmpty(campaignIds)) return List.of();

        List<CampaignContact> campaignContacts = isParticipating ?
                campaignContactRepository.findAllByCampaignIdInAndParticipatingTrue(campaignIds) :
                campaignContactRepository.findAllByCampaignIdIn(campaignIds);

        if (CollectionUtils.isEmpty(campaignContacts)) return List.of();

        List<Long> contactIds = campaignContacts.stream()
                .map(CampaignContact::getContactId)
                .distinct()
                .toList();
        List<LeadContactDto> leadContacts = leadContactService.getLeadContactsByIds(contactIds);

        Map<Long, LeadContactDto> contactIdToLeadContactMap = leadContacts.stream()
                .collect(Collectors.toMap(LeadContactDto::getId, lc -> lc));

        List<String> emails = leadContacts.stream()
                .map(LeadContactDto::getEmail)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Set<String> tenantExistingContactEmails = tenantContactService.getExistingEmails(tenantId, emails);

        Map<Long, TenantContactMetadata> metadataMap = tenantContactMetadataRepository
                .findAllByTenantIdAndLeadContactIdIn(tenantId, contactIds)
                .stream()
                .collect(Collectors.toMap(TenantContactMetadata::getLeadContactId, m -> m));

        return campaignContacts.stream()
                .map(contact -> {
                    LeadContactDto leadContactDto = contactIdToLeadContactMap.getOrDefault(
                            contact.getContactId(),
                            new LeadContactDto()
                    );
                    boolean tenantContactExisting = leadContactDto.getEmail() != null && tenantExistingContactEmails.contains(leadContactDto.getEmail());
                    CampaignContactInfoDto dto = CampaignContactInfoDto.fromEntity(contact, leadContactDto, tenantContactExisting);
                    TenantContactMetadata metadata = metadataMap.get(contact.getContactId());
                    if (metadata != null) {
                        applyTenantContactMetadata(dto, metadata);
                    }
                    return dto;
                })
                .toList();
    }

    public void updateContactIdsOfCampaign(Long tenantId, Long campaignId, Set<Long> contactIds, boolean skipCrmCheck) {
        if (tenantId == null) {
            log.warn("Tenant ID is null while updating campaign contacts for campaign {}", campaignId);
            return;
        }
        if (CollectionUtils.isEmpty(contactIds)) {
            log.info("No contact IDs provided to update for campaign {}", campaignId);
            return;
        }
        List<CampaignContact> existingContacts = campaignContactRepository.findAllByCampaignId(campaignId);
        Set<Long> existingContactIds = existingContacts.stream()
                .map(CampaignContact::getContactId)
                .collect(Collectors.toSet());

        List<Long> newContactIds = contactIds.stream()
                .filter(contactId -> !existingContactIds.contains(contactId))
                .toList();
        List<Long> contactIdsToDelete = existingContacts.stream()
                .filter(contact -> !contactIds.contains(contact.getContactId()))
                .map(CampaignContact::getId)
                .toList();

        int added = saveNewContactsForCampaign(tenantId, campaignId, newContactIds, skipCrmCheck);
        if (!CollectionUtils.isEmpty(contactIdsToDelete)) {
            campaignContactRepository.deleteAllById(contactIdsToDelete);
        }
        log.info("Updated campaign {} contacts. Added: {}, Deleted: {}", campaignId, added, contactIdsToDelete.size());
    }

    public void addContactsToCampaign(Long tenantId, Long campaignId, List<Long> contactIds) {
        if (CollectionUtils.isEmpty(contactIds)) {
            log.info("No contact IDs provided to add for campaign {}", campaignId);
            return;
        }

        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));

        if (!ALLOWED_ADD_STATUSES.contains(campaign.getStatus())) {
            throw new BadRequestException(
                    "Cannot add contacts to a campaign with status: " + campaign.getStatus()
            );
        }

        Set<Long> existingContactIds = campaignContactRepository.findAllByCampaignIdAndContactIdIn(campaignId, contactIds)
                .stream()
                .map(CampaignContact::getContactId)
                .collect(Collectors.toSet());

        List<Long> newContactIds = contactIds
                .stream()
                .distinct()
                .filter(id -> !existingContactIds.contains(id))
                .toList();

        int added = saveNewContactsForCampaign(tenantId, campaignId, newContactIds, false);
        log.info("Added {} new contacts to campaign {}", added, campaignId);
    }

    public void updateCampaignContacts(Long campaignId, List<Long> selectedContactIds, List<Long> excludedContactIds) {
        updateCampaignContactParticipation(campaignId, selectedContactIds, true);
        updateCampaignContactParticipation(campaignId, excludedContactIds, false);
    }


    public CampaignContactDto saveCampaignContact(CampaignContactDto campaignContactDto) {
        CampaignContact campaignContact = campaignContactDto.toEntity();
        campaignContactRepository.save(campaignContact);
        return CampaignContactDto.fromEntity(campaignContact);
    }

    public boolean isCurrentStepOngoing(Long campaignId, int stepNumber) {
        return campaignContactRepository.existsByCampaignIdAndStatusAndCurrentStepAndParticipatingTrue(
                campaignId,
                CampaignContactStatus.ACTIVE,
                stepNumber
        );
    }

    public List<CampaignContactDto> getCampaignContactsByCampaignId(Long campaignId) {
        List<CampaignContact> campaignContacts = campaignContactRepository.findAllByCampaignIdAndParticipatingTrue(campaignId);
        return campaignContacts.stream()
                .map(CampaignContactDto::fromEntity)
                .toList();
    }

    private int saveNewContactsForCampaign(Long tenantId, Long campaignId, List<Long> newContactIds, boolean skipCrmCheck) {
        if (CollectionUtils.isEmpty(newContactIds)) {
            return 0;
        }
        List<LeadContactDto> newLeadContacts = leadContactService.getLeadContactsByIds(newContactIds);

        Set<String> tenantExistingContactEmails = Set.of();
        if (!skipCrmCheck) {
            List<String> emailList = newLeadContacts.stream()
                    .map(LeadContactDto::getEmail)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            tenantExistingContactEmails = tenantContactService.getExistingEmails(tenantId, emailList);
        }

        final Set<String> existingEmails = tenantExistingContactEmails;
        List<CampaignContact> newContacts = newLeadContacts.stream()
                .map(leadContact -> CampaignContact.builder()
                        .campaignId(campaignId)
                        .contactId(leadContact.getId())
                        .participating(StringUtils.hasText(leadContact.getEmail()) && !existingEmails.contains(leadContact.getEmail()))
                        .currentStep(0)
                        .replyReceived(false)
                        .status(CampaignContactStatus.PENDING)
                        .build())
                .toList();
        campaignContactRepository.saveAll(newContacts);
        return newContacts.size();
    }

    @Async
    @EventListener
    public void handleCampaignLaunchedEvent(CampaignLaunchedEvent event) {
        CampaignDto campaignDto = event.getCampaignDto();
        List<CampaignContact> campaignContacts = campaignContactRepository.findAllByCampaignIdAndParticipatingTrue(campaignDto.getId());
        List<Long> contactIds = campaignContacts.stream()
                .map(CampaignContact::getContactId)
                .toList();
        // Create outreach statuses BEFORE activating contacts to avoid a race condition
        // where the orchestrator picks up a contact before its outreach status exists.
        // Returns contact IDs that are ineligible (unsubscribed, bounced, paused).
        Set<Long> ineligibleContactIds = contactOutreachStatusService.handleCampaignLaunchedEvent(
                campaignDto.getTenantId(),
                contactIds,
                campaignDto.getId()
        );
        for (CampaignContact contact : campaignContacts) {
            if (ineligibleContactIds.contains(contact.getContactId())) {
                contact.setStatus(CampaignContactStatus.UNSUBSCRIBED);
                contact.setNextSendAt(null);
            } else {
                contact.setCurrentStep(1);
                contact.setNextSendAt(LocalDateTime.now());
                contact.setStatus(CampaignContactStatus.ACTIVE);
            }
        }
        campaignContactRepository.saveAll(campaignContacts);

        if (!ineligibleContactIds.isEmpty()) {
            autoCompleteCampaignsIfNoActionableContacts(Set.of(campaignDto.getId()));
        }
    }

    @Async
    @EventListener
    public void handleCampaignPausedEvent(CampaignPausedEvent event) {
        Long campaignId = event.getCampaignDto().getId();
        long updatedCount = campaignContactRepository.findByCampaignIdAndStatusAndParticipatingTrue(
                campaignId,
                CampaignContactStatus.ACTIVE,
                CampaignContactStatus.PAUSED
        );
        log.info("Paused {} campaign contacts for campaign {}", updatedCount, campaignId);
    }

    @Async
    @EventListener
    public void handleCampaignResumedEvent(CampaignResumeEvent event) {
        Long campaignId = event.getCampaignDto().getId();
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));

        // Resume contacts that were active before the campaign was paused
        long resumedCount = campaignContactRepository.findByCampaignIdAndStatusAndParticipatingTrue(
                campaignId,
                CampaignContactStatus.PAUSED,
                CampaignContactStatus.ACTIVE
        );

        // Activate contacts that were added while the campaign was paused
        LocalDateTime now = LocalDateTime.now();
        List<CampaignContact> pendingContacts = campaignContactRepository.findAllByCampaignIdAndStatusAndParticipatingTrue(campaignId, CampaignContactStatus.PENDING);

        // Create outreach statuses BEFORE activating contacts to avoid a race condition
        // where the orchestrator picks up a contact before its outreach status exists.
        // Returns contact IDs that are ineligible (unsubscribed, bounced, paused).
        List<Long> contactIds = pendingContacts.stream()
                .map(CampaignContact::getContactId)
                .toList();
        Set<Long> ineligibleContactIds = contactOutreachStatusService.handleCampaignLaunchedEvent(
                campaign.getTenantId(),
                contactIds,
                campaignId
        );

        if (!CollectionUtils.isEmpty(pendingContacts)) {
            for (CampaignContact contact : pendingContacts) {
                if (ineligibleContactIds.contains(contact.getContactId())) {
                    contact.setStatus(CampaignContactStatus.UNSUBSCRIBED);
                    contact.setNextSendAt(null);
                    continue;
                }
                contact.setCurrentStep(1);
                contact.setNextSendAt(now);
                contact.setStatus(CampaignContactStatus.ACTIVE);
            }
            campaignContactRepository.saveAll(pendingContacts);
        }

        log.info("Resumed {} paused contacts and activated {} pending contacts for campaign {}", resumedCount, pendingContacts.size(), campaignId);
    }

    public Page<CampaignContactDto> getCampaignContactsByPlatformAndStatus(
            MailBoxType platform, EmailDeliveryStatus status, LocalDateTime since, Pageable pageable) {
        return campaignContactRepository.findAllByEmailDataEmailPlatformAndEmailDataEmailDeliveryStatus(
                platform.name(),
                status.name(),
                since,
                pageable
        ).map(CampaignContactDto::fromEntity);
    }

    private void updateCampaignContactParticipation(Long campaignId, List<Long> contactIds, boolean participating) {
        if (CollectionUtils.isEmpty(contactIds)) {
            return;
        }
        List<CampaignContact> campaignContacts = campaignContactRepository.findAllByCampaignIdAndIdIn(campaignId, contactIds);
        for (CampaignContact contact : campaignContacts) {
            contact.setParticipating(participating);
        }
        campaignContactRepository.saveAll(campaignContacts);
    }

    private CampaignContactInfoDto mapToCampaignContactInfoDto(CampaignContact campaignContact) {
        LeadContactDto leadContactDto = leadContactService.getContactById(campaignContact.getContactId());
        CampaignContactInfoDto dto = CampaignContactInfoDto.fromEntity(campaignContact, leadContactDto);

        campaignRepository.findById(campaignContact.getCampaignId())
                .map(Campaign::getTenantId)
                .flatMap(tenantId -> tenantContactMetadataRepository
                        .findByTenantIdAndLeadContactId(tenantId, campaignContact.getContactId()))
                .ifPresent(metadata -> applyTenantContactMetadata(dto, metadata));
        return dto;
    }

    private void applyTenantContactMetadata(CampaignContactInfoDto dto, TenantContactMetadata metadata) {
        dto.setBdName(metadata.getBdName());
        dto.setBdEmail(metadata.getBdEmail());
        dto.setBdPhone(metadata.getBdPhone());
        dto.setIsrName(metadata.getIsrName());
        dto.setIsrEmail(metadata.getIsrEmail());
        dto.setIsrPhone(metadata.getIsrPhone());
    }

    @Async
    @EventListener
    public void handleContactUnsubscribedEvent(ContactUnsubscribedEvent event) {
        List<CampaignContact> activeContacts = campaignContactRepository
                .findAllByContactIdAndCampaignIdInAndStatus(
                        event.getContactId(),
                        event.getCampaignIds(),
                        CampaignContactStatus.ACTIVE
                );
        if (CollectionUtils.isEmpty(activeContacts)) {
            return;
        }
        for (CampaignContact contact : activeContacts) {
            contact.setStatus(CampaignContactStatus.UNSUBSCRIBED);
            contact.setNextSendAt(null);
        }
        campaignContactRepository.saveAll(activeContacts);
        log.info("Marked {} campaign contacts as UNSUBSCRIBED for contactId={}", activeContacts.size(), event.getContactId());

        Set<Long> affectedCampaignIds = activeContacts.stream()
                .map(CampaignContact::getCampaignId)
                .collect(Collectors.toSet());
        autoCompleteCampaignsIfNoActionableContacts(affectedCampaignIds);
    }

    private void autoCompleteCampaignsIfNoActionableContacts(Set<Long> campaignIds) {
        for (Long campaignId : campaignIds) {
            boolean hasActionableContacts =
                    !CollectionUtils.isEmpty(campaignContactRepository.findAllByCampaignIdAndStatusAndParticipatingTrue(campaignId, CampaignContactStatus.ACTIVE))
                    || !CollectionUtils.isEmpty(campaignContactRepository.findAllByCampaignIdAndStatusAndParticipatingTrue(campaignId, CampaignContactStatus.PENDING));
            if (hasActionableContacts) {
                continue;
            }
            campaignRepository.findById(campaignId)
                    .filter(c -> c.getStatus() == CampaignStatus.RUNNING)
                    .ifPresent(campaign -> {
                        campaign.setStatus(CampaignStatus.COMPLETED);
                        campaignRepository.save(campaign);
                        eventPublisher.publishEvent(new CampaignCompletedEvent(this, CampaignDto.fromEntity(campaign)));
                        log.info("Campaign {} auto-completed: no actionable contacts remaining", campaignId);
                    });
        }
    }

    @Async
    @EventListener
    public void handleCampaignDeletedEvent(CampaignDeletedEvent event) {
        CampaignDto campaignDto = event.getCampaignDto();
        List<CampaignContact> campaignContacts = campaignContactRepository.findAllByCampaignId(campaignDto.getId());
        if (!CollectionUtils.isEmpty(campaignContacts)) {
            campaignContactRepository.deleteAll(campaignContacts);
            log.info("Deleted {} campaign contacts for deleted campaign {}", campaignContacts.size(), campaignDto.getId());
        }
    }
}
