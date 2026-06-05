package ai.leadplus.application.contactoutreachstatuses;

import ai.leadplus.application.campaigncontacts.CampaignContactInfoDto;
import ai.leadplus.application.campaignorchestrator.CampaignEmailSentEvent;
import ai.leadplus.application.campaigns.CampaignCompletedEvent;
import ai.leadplus.application.campaigns.CampaignDto;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.application.leadcontact.LeadContactService;
import ai.leadplus.application.leads.LeadDto;
import ai.leadplus.domain.contactoutreachstatuses.ContactOutreachStatus;
import ai.leadplus.domain.contactoutreachstatuses.ContactOutreachStatusRepository;
import ai.leadplus.domain.contactoutreachstatuses.GlobalOutreachStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactOutreachStatusService {

    @Value("${campaign.contacts.last-email-throttle-days}")
    private int lastEmailThrottleDays;

    @Value("${campaign.contacts.sequence-cooldown-days}")
    private int sequenceCooldownDays;

    private final ContactOutreachStatusRepository contactOutreachStatusRepository;
    private final LeadContactService leadContactService;
    private final ApplicationEventPublisher eventPublisher;

    private static final List<GlobalOutreachStatus> NON_ELIGIBLE_STATUSES = List.of(
            GlobalOutreachStatus.PAUSED,
            GlobalOutreachStatus.BOUNCED,
            GlobalOutreachStatus.UNSUBSCRIBED
    );

    /**
     * Prepares outreach statuses for a campaign launch.
     * Creates new statuses for contacts without one, and adds the campaign ID to existing ones.
     * Skips contacts with non-eligible statuses (UNSUBSCRIBED, BOUNCED, PAUSED).
     *
     * @return set of contact IDs that are ineligible and should not be activated in the campaign
     */
    public Set<Long> handleCampaignLaunchedEvent(Long tenantId, List<Long> contactIds, Long campaignId) {
        List<ContactOutreachStatus> existingStatuses = contactOutreachStatusRepository.findAllByTenantIdAndContactIdIn(tenantId, contactIds);
        Map<Long, ContactOutreachStatus> statusMap = existingStatuses.stream()
                .collect(Collectors.toMap(ContactOutreachStatus::getContactId, status -> status));

        Set<Long> ineligibleContactIds = new HashSet<>();
        List<ContactOutreachStatus> newStatuses = new ArrayList<>();
        contactIds.forEach(contactId -> {
            ContactOutreachStatus status = statusMap.get(contactId);
            if (status != null) {
                if (NON_ELIGIBLE_STATUSES.contains(status.getStatus())) {
                    ineligibleContactIds.add(contactId);
                    return;
                }
                addNewCampaignId(status, campaignId);
            } else {
                status = ContactOutreachStatus.builder()
                        .tenantId(tenantId)
                        .contactId(contactId)
                        .status(GlobalOutreachStatus.ACTIVE)
                        .currentCampaignIds(new ArrayList<>(List.of(campaignId)))
                        .unsubscribeToken(UUID.randomUUID().toString())
                        .build();
            }
            newStatuses.add(status);
        });
        contactOutreachStatusRepository.saveAll(newStatuses);
        log.info("Contact Outreach Status updated with current campaign ID for tenantId: {}, campaignId: {}, eligible: {}, ineligible: {}",
                tenantId, campaignId, newStatuses.size(), ineligibleContactIds.size());
        return ineligibleContactIds;
    }

    public void createContactOutreachStatus(Long tenantId, Long contactId, GlobalOutreachStatus status) {
        String unsubscribeToken = UUID.randomUUID().toString();
        ContactOutreachStatus contactOutreachStatus = ContactOutreachStatus.builder()
                .tenantId(tenantId)
                .contactId(contactId)
                .status(status)
                .unsubscribeToken(unsubscribeToken)
                .build();
        contactOutreachStatusRepository.save(contactOutreachStatus);
    }

    public List<LeadContactDto> filterCampaignEligibleContacts(Long tenantId, List<LeadContactDto> contacts) {
        if (CollectionUtils.isEmpty(contacts)) {
            return List.of();
        }
        List<Long> contactIds = contacts.stream()
                .map(LeadContactDto::getId)
                .filter(Objects::nonNull)
                .toList();
        Set<Long> ineligibleContactIds = getIneligibleContactIds(tenantId, contactIds);
        return contacts.stream()
                .filter(contact -> contact.getId() != null)
                .filter(contact -> StringUtils.hasText(contact.getEmail()))
                .filter(contact -> !ineligibleContactIds.contains(contact.getId()))
                .toList();
    }

    public Set<Long> getIneligibleContactIds(Long tenantId) {
        LocalDateTime emailCutoff = LocalDateTime.now().minusDays(lastEmailThrottleDays);
        LocalDateTime cooldownCutoff = LocalDateTime.now().minusDays(sequenceCooldownDays);
        return new HashSet<>(contactOutreachStatusRepository
                .findIneligibleContactIds(tenantId, emailCutoff, cooldownCutoff));
    }

    public Set<Long> getIneligibleContactIds(Long tenantId, List<Long> contactIds) {
        if (CollectionUtils.isEmpty(contactIds)) {
            return Set.of();
        }
        LocalDateTime emailCutoff = LocalDateTime.now().minusDays(lastEmailThrottleDays);
        LocalDateTime cooldownCutoff = LocalDateTime.now().minusDays(sequenceCooldownDays);
        return new HashSet<>(contactOutreachStatusRepository
                .findIneligibleContactIdsByTenantIdAndContactIdIn(tenantId, contactIds, emailCutoff, cooldownCutoff));
    }

    public List<LeadDto> filterCampaignEligibleLeads(Long tenantId, List<LeadDto> leads) {
        if (CollectionUtils.isEmpty(leads)) {
            return List.of();
        }
        List<Long> contactIds = leads.stream()
                .map(LeadDto::getId)
                .filter(Objects::nonNull)
                .toList();
        Set<Long> ineligibleContactIds = getIneligibleContactIds(tenantId, contactIds);
        return leads.stream()
                .filter(lead -> lead.getId() != null)
                .filter(lead -> StringUtils.hasText(lead.getEmail()))
                .filter(lead -> !ineligibleContactIds.contains(lead.getId()))
                .toList();
    }

    public boolean validateContactEligibility(Long tenantId, CampaignContactInfoDto campaignContact) {
        ContactOutreachStatus contactOutreachStatus = findContactOutReachByTenantIdAndContactId(tenantId, campaignContact.getContactId());
        return isContactEligible(
                tenantId,
                campaignContact.getEmail(),
                campaignContact.getContactId(),
                campaignContact.getCampaignId(),
                contactOutreachStatus,
                false,
                campaignContact.getCurrentStep()
        );
    }

    public void unsubscribeByToken(String unsubscribeToken) {
        ContactOutreachStatus contactOutreachStatus =
                contactOutreachStatusRepository.findByUnsubscribeToken(unsubscribeToken)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Invalid unsubscribe token"));

        if (contactOutreachStatus.getStatus() == GlobalOutreachStatus.UNSUBSCRIBED) {
            log.info("Contact already unsubscribed. contactId={}", contactOutreachStatus.getContactId());
            return;
        }

        List<Long> campaignIds = contactOutreachStatus.getCurrentCampaignIds();

        contactOutreachStatus.setStatus(GlobalOutreachStatus.UNSUBSCRIBED);
        contactOutreachStatus.setCurrentCampaignIds(new ArrayList<>());
        contactOutreachStatusRepository.save(contactOutreachStatus);

        log.info("Successfully unsubscribed contactId={} tenantId={}",
                contactOutreachStatus.getContactId(),
                contactOutreachStatus.getTenantId());

        if (!CollectionUtils.isEmpty(campaignIds)) {
            eventPublisher.publishEvent(new ContactUnsubscribedEvent(
                    this,
                    contactOutreachStatus.getTenantId(),
                    contactOutreachStatus.getContactId(),
                    campaignIds
            ));
        }
    }

    public Map<Long, ContactOutreachStatusDto> getContactOutreachStatusDtoMap(Long tenantId, List<Long> contactIds) {
        List<ContactOutreachStatus> outreachStatuses = contactOutreachStatusRepository.findAllByTenantIdAndContactIdIn(tenantId, contactIds);
        return outreachStatuses.stream()
                .collect(Collectors.toMap(
                        ContactOutreachStatus::getContactId,
                        ContactOutreachStatusDto::fromEntity
                ));
    }

    private boolean isContactEligible(Long tenantId,
                                      String email,
                                      Long leadContactId,
                                      Long campaignId,
                                      ContactOutreachStatus contactOutreachStatus,
                                      boolean checkCurrentCampaignId,
                                      int currentStep
    ) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        if (Objects.isNull(contactOutreachStatus)) {
            return true;
        }
        if (checkCurrentCampaignId && !CollectionUtils.isEmpty(contactOutreachStatus.getCurrentCampaignIds())) {
            return false;
        }
        if (NON_ELIGIBLE_STATUSES.contains(contactOutreachStatus.getStatus())) {
            return false;
        }
        if (Objects.equals(contactOutreachStatus.getStatus(), GlobalOutreachStatus.ACTIVE)) {
            boolean sameCampaignActive = campaignId != null
                    && !CollectionUtils.isEmpty(contactOutreachStatus.getCurrentCampaignIds())
                    && contactOutreachStatus.getCurrentCampaignIds().contains(campaignId);
            // Only bypass throttle for follow-up steps (step > 1) within the same campaign.
            // Step 1 of a new campaign must still respect the lastEmailAt throttle.
            if (sameCampaignActive && currentStep > 1) {
                return true;
            }
            LocalDateTime lastEmailAt = contactOutreachStatus.getLastEmailAt();
            if (Objects.isNull(lastEmailAt)) {
                return true;
            }
            return lastEmailAt.plusDays(lastEmailThrottleDays).isBefore(LocalDateTime.now());
        }
        if (Objects.equals(contactOutreachStatus.getStatus(), GlobalOutreachStatus.COMPLETED)) {
            LocalDateTime sequenceCompletedAt = contactOutreachStatus.getSequenceCompletedAt();
            if (Objects.isNull(sequenceCompletedAt)) {
                return true;
            }
            return sequenceCompletedAt.plusDays(sequenceCooldownDays).isBefore(LocalDateTime.now());
        }
        return false;
    }

    @EventListener
    public void handleCampaignEmailSendEvent(CampaignEmailSentEvent event) {
        CampaignContactInfoDto campaignContact = event.getCampaignContactInfoDto();
        Long tenantId = event.getTenantId();
        ContactOutreachStatus contactOutreachStatus = findContactOutReachByTenantIdAndContactId(tenantId, campaignContact.getContactId());
        contactOutreachStatus.setLastEmailAt(LocalDateTime.now());
        contactOutreachStatusRepository.save(contactOutreachStatus);
    }

    @Async
    @EventListener
    public void handleCampaignCompletedEvent(CampaignCompletedEvent event) {
        CampaignDto campaignDto = event.getCampaignDto();

        List<ContactOutreachStatus> outreachStatuses = contactOutreachStatusRepository
                .findAllByTenantIdAndCurrentCampaignIdsContaining(campaignDto.getTenantId(), campaignDto.getId());
        outreachStatuses.forEach(status -> {
            removeCampaignId(status, campaignDto.getId());
            status.setSequenceCompletedAt(LocalDateTime.now());
        });
        contactOutreachStatusRepository.saveAll(outreachStatuses);
    }

    public ContactOutreachStatus getContactOutreachStatus(Long tenantId, Long contactId) {
        ContactOutreachStatus status = findContactOutReachByTenantIdAndContactId(tenantId, contactId);

        if (!StringUtils.hasText(status.getUnsubscribeToken())) {
            status.setUnsubscribeToken(UUID.randomUUID().toString());
            status = contactOutreachStatusRepository.save(status);

            log.info("Backfilled unsubscribeToken for tenantId={} contactId={}", tenantId, contactId);
        }

        return status;
    }

    public Page<LeadContactDto> getUnsubscribedContacts(Long tenantId, Pageable pageable) {
        Page<ContactOutreachStatus> statusPage = contactOutreachStatusRepository
                .findAllByTenantIdAndStatus(tenantId, GlobalOutreachStatus.UNSUBSCRIBED, pageable);

        List<Long> contactIds = statusPage.getContent().stream()
                .map(ContactOutreachStatus::getContactId)
                .filter(Objects::nonNull)
                .toList();

        List<LeadContactDto> contacts = CollectionUtils.isEmpty(contactIds) ?
                List.of() : leadContactService.getLeadContactsByIds(contactIds);

        Map<Long, LeadContactDto> idToContact = contacts.stream()
                .collect(Collectors.toMap(LeadContactDto::getId, c -> c));

        List<LeadContactDto> ordered = statusPage.getContent().stream()
                .map(ContactOutreachStatus::getContactId)
                .map(idToContact::get)
                .filter(Objects::nonNull)
                .toList();

        return new PageImpl<>(ordered, pageable, statusPage.getTotalElements());
    }

    public LocalDateTime findLatestCampaignEmailAtByTenantId(Long tenantId) {
        return contactOutreachStatusRepository.findTopByTenantIdAndLastEmailAtNotNullOrderByLastEmailAtDesc(tenantId)
                .map(ContactOutreachStatus::getLastEmailAt)
                .orElse(null);
    }

    private ContactOutreachStatus findContactOutReachByTenantIdAndContactId(Long tenantId, Long contactId) {
        return contactOutreachStatusRepository.findByTenantIdAndContactId(tenantId, contactId)
                .orElseThrow(() -> new ResourceNotFoundException("No contact with id " + contactId + " in tenant " + tenantId));
    }

    private void addNewCampaignId(ContactOutreachStatus status, Long campaignId) {
        if (campaignId == null) {
            return;
        }
        List<Long> currentCampaignIds = status.getCurrentCampaignIds();
        if (CollectionUtils.isEmpty(currentCampaignIds)) {
            currentCampaignIds = new ArrayList<>();
        }
        currentCampaignIds.add(campaignId);
        status.setCurrentCampaignIds(currentCampaignIds);
    }

    private void removeCampaignId(ContactOutreachStatus status, Long campaignId) {
        if (campaignId == null) {
            return;
        }
        List<Long> currentCampaignIds = status.getCurrentCampaignIds();
        if (!CollectionUtils.isEmpty(currentCampaignIds)) {
            currentCampaignIds.remove(campaignId);
            status.setCurrentCampaignIds(currentCampaignIds);
        }
    }
}
