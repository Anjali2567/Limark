package ai.leadplus.application.campaignemails;

import ai.leadplus.application.campaigns.CampaignDeletedEvent;
import ai.leadplus.application.campaigns.CampaignDto;
import ai.leadplus.application.campaigns.CampaignLaunchedEvent;
import ai.leadplus.application.campaigns.CampaignPausedEvent;
import ai.leadplus.application.campaigns.SendingWindowService;
import ai.leadplus.application.campaigns.CampaignResumeEvent;
import ai.leadplus.application.campaigncontacts.CampaignContactInfoDto;
import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.application.leadcontact.LeadContactService;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.campaignemails.CampaignEmail;
import ai.leadplus.domain.campaignemails.CampaignEmailRepository;
import ai.leadplus.domain.campaignemails.CampaignEmailStatus;
import ai.leadplus.domain.campaigncontacts.CampaignContact;
import ai.leadplus.domain.campaigncontacts.CampaignContactRepository;
import ai.leadplus.domain.campaigncontacts.CampaignContactStatus;
import ai.leadplus.domain.campaigns.Campaign;
import ai.leadplus.domain.campaigns.CampaignStatus;
import ai.leadplus.domain.campaigns.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignEmailService {

    private final CampaignRepository campaignRepository;
    private final CampaignEmailRepository campaignEmailRepository;
    private final CampaignContactRepository campaignContactRepository;
    private final LeadContactService leadContactService;
    private final SendingWindowService sendingWindowService;

    private static final List<String> DEFAULT_SUBJECTS = List.of(
            "Partnership Opportunity with LeadPlus",
            "Touching base regarding my previous email",
            "Last attempt to connect"
    );

    private static final List<String> DEFAULT_BODY_TEMPLATE = List.of(
            """
                    Hi {firstName},
                    
                    I noticed that {companyName} is leading the way in the {industry} sector, and I wanted to reach out...""",
            """
                    Hi {firstName},
                    
                    I just wanted to float this to the top of your inbox in case you missed my previous message...""",
            """
                    Hi {firstName},
                    
                    I haven't heard back from you, so I'll assume now isn't the best time..."""
    );

    private static final List<CampaignEmailStatus> NON_UPDATABLE_STATUSES =
            List.of(CampaignEmailStatus.RUNNING, CampaignEmailStatus.COMPLETED, CampaignEmailStatus.PAUSED);

    public List<CampaignEmailDto> getEmailSequencesByCampaignId(Long campaignId) {
        List<CampaignEmail> campaignEmails = campaignEmailRepository.findAllByCampaignId(campaignId);
        return campaignEmails.stream()
                .map(CampaignEmailDto::fromEntity)
                .toList();
    }

    public Optional<CampaignEmailDto> getOptionalCampaignEmailByCampaignIdAndStepNumber(Long campaignId, int stepNumber) {
        Optional<CampaignEmail> campaignEmailOpt = campaignEmailRepository.findByCampaignIdAndStepNumber(campaignId, stepNumber);
        return campaignEmailOpt.map(CampaignEmailDto::fromEntity);
    }

    public CampaignEmailDto getCampaignEmailByCampaignIdAndStepNumber(Long campaignId, int stepNumber) {
        CampaignEmail campaignEmail = findCampaignEmailByCampaignIdAndStepNumber(campaignId, stepNumber);
        return CampaignEmailDto.fromEntity(campaignEmail);
    }

    private CampaignEmail findCampaignEmailByCampaignIdAndStepNumber(Long campaignId, int stepNumber) {
        return campaignEmailRepository.findByCampaignIdAndStepNumber(campaignId, stepNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign Email not found for campaignId: " + campaignId + " and stepNumber: " + stepNumber));
    }

    public void createDefaultEmailSequence(Long campaignId) {
        if (campaignEmailRepository.existsByCampaignId(campaignId)) {
            return;
        }
        CampaignEmail campaignEmail = CampaignEmail.builder()
                .campaignId(campaignId)
                .stepNumber(1)
                .subject(DEFAULT_SUBJECTS.getFirst())
                .bodyTemplate(DEFAULT_BODY_TEMPLATE.getFirst())
                .delayDays(0)
                .status(CampaignEmailStatus.PENDING)
                .build();
        campaignEmailRepository.save(campaignEmail);
    }

    public void configureEmailSequences(Long campaignId) {
        if (campaignEmailRepository.existsByCampaignId(campaignId)) {
            return;
        }
        validateCampaignNotRunning(campaignId);
        int stepCount = 3;
        List<Integer> defaultDelays = List.of(0, 3, 4);
        List<CampaignEmail> campaignEmails = new ArrayList<>();
        for (int i = 0; i < stepCount; i++) {
            CampaignEmail campaignEmail = CampaignEmail.builder()
                    .campaignId(campaignId)
                    .stepNumber(i + 1)
                    .subject(DEFAULT_SUBJECTS.get(i))
                    .bodyTemplate(DEFAULT_BODY_TEMPLATE.get(i))
                    .delayDays(defaultDelays.get(i))
                    .status(CampaignEmailStatus.PENDING)
                    .build();
            campaignEmails.add(campaignEmail);
        }
        campaignEmailRepository.saveAll(campaignEmails);
    }

    public void updateNoOfSequences(Long campaignId, int noOfSequences) {
        validateCampaignNotRunning(campaignId);
        List<CampaignEmail> existingEmails = new ArrayList<>(campaignEmailRepository.findAllByCampaignId(campaignId));
        int existingCount = existingEmails.size();

        if (noOfSequences > existingCount) {
            List<CampaignEmail> newEmails = new ArrayList<>();
            for (int i = 0; i < noOfSequences - existingCount; i++) {
                int stepNumber = existingCount + i + 1;
                String subject = stepNumber == 1 ? DEFAULT_SUBJECTS.getFirst() : DEFAULT_SUBJECTS.get(1);
                String bodyTemplate = stepNumber == 1 ? DEFAULT_BODY_TEMPLATE.getFirst() : DEFAULT_BODY_TEMPLATE.get(1);

                CampaignEmail campaignEmail = CampaignEmail.builder()
                        .campaignId(campaignId)
                        .stepNumber(stepNumber)
                        .subject(subject)
                        .bodyTemplate(bodyTemplate)
                        .delayDays(3)
                        .status(CampaignEmailStatus.PENDING)
                        .build();
                newEmails.add(campaignEmail);
            }
            campaignEmailRepository.saveAll(newEmails);
        } else if (noOfSequences < existingCount) {
            existingEmails.sort(Comparator.comparingInt(CampaignEmail::getStepNumber));
            List<CampaignEmail> emailsToDelete = existingEmails.subList(noOfSequences, existingCount);
            campaignEmailRepository.deleteAll(emailsToDelete);
        }
    }

    public CampaignEmailDto updateCampaignEmail(Long id, CampaignEmailDto campaignEmailDto) {
        CampaignEmail existingCampaignEmail = findCampaignEmailById(id);

        if (NON_UPDATABLE_STATUSES.contains(existingCampaignEmail.getStatus())) {
            throw new BadRequestException("Cannot edit completed or running emails");
        }

        existingCampaignEmail.setSubject(campaignEmailDto.getSubject());
        existingCampaignEmail.setBodyTemplate(campaignEmailDto.getBodyTemplate());
        existingCampaignEmail.setDelayDays(campaignEmailDto.getDelayDays());
        existingCampaignEmail.setAttachmentIds(campaignEmailDto.getAttachmentIds());
        campaignEmailRepository.save(existingCampaignEmail);
        return CampaignEmailDto.fromEntity(existingCampaignEmail);
    }

    public void saveCampaignEmail(CampaignEmailDto campaignEmailDto) {
        CampaignEmail campaignEmail = campaignEmailDto.toEntity();
        campaignEmailRepository.save(campaignEmail);
    }

    @Async
    @EventListener
    public void handleCampaignLaunchedEvent(CampaignLaunchedEvent event) {
        Long campaignId = event.getCampaignDto().getId();
        CampaignEmail campaignEmail = findCampaignEmailByCampaignIdAndStepNumber(campaignId, 1);
        campaignEmail.setStatus(CampaignEmailStatus.RUNNING);
        campaignEmailRepository.save(campaignEmail);
    }

    @Async
    @EventListener
    public void handleCampaignPausedEvent(CampaignPausedEvent event) {
        Long campaignId = event.getCampaignDto().getId();
        long updatedCount = campaignEmailRepository.findByCampaignIdAndStatus(
                campaignId,
                CampaignEmailStatus.RUNNING,
                CampaignEmailStatus.PAUSED
        );
        log.info("Paused {} campaign emails for campaign {}", updatedCount, campaignId);
    }

    @Async
    @EventListener
    public void handleCampaignResumedEvent(CampaignResumeEvent event) {
        Long campaignId = event.getCampaignDto().getId();
        long updatedCount = campaignEmailRepository.findByCampaignIdAndStatus(
                campaignId,
                CampaignEmailStatus.PAUSED,
                CampaignEmailStatus.RUNNING
        );
        log.info("Resumed {} campaign emails for campaign {}", updatedCount, campaignId);
    }

    @Async
    @EventListener
    public void handleCampaignDeletedEvent(CampaignDeletedEvent event) {
        Long campaignId = event.getCampaignDto().getId();
        List<CampaignEmail> emailsToDelete = campaignEmailRepository.findAllByCampaignId(campaignId);
        campaignEmailRepository.deleteAll(emailsToDelete);
        int deletedCount = emailsToDelete.size();
        log.info("Deleted {} campaign emails for campaign {}", deletedCount, campaignId);
    }

    public Map<Long, Integer> getCampaignEmailCountByCampaignIds(List<Long> campaignIds) {
        List<CampaignEmailCountDto> campaignEmailCounts = campaignEmailRepository.countCampaignEmailByCampaignIds(campaignIds);
        return campaignEmailCounts.stream()
                .collect(Collectors.toMap(
                        CampaignEmailCountDto::getCampaignId,
                        CampaignEmailCountDto::getCount
                ));
    }

    private CampaignEmail findCampaignEmailById(Long id) {
        return campaignEmailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign Email not found with id: " + id));
    }

    public CampaignEmailDto createBasicCampaignEmail(Long campaignId) {
        validateCampaignById(campaignId);

        Optional<CampaignEmail> previousEmail = campaignEmailRepository
                .findTopByCampaignIdOrderByStepNumberDesc(campaignId);
        CampaignEmail previous = previousEmail.orElse(null);
        int lastStepNumber = previousEmail.map(CampaignEmail::getStepNumber).orElse(0);

        CampaignEmail campaignEmail = CampaignEmail.builder()
                .campaignId(campaignId)
                .stepNumber(lastStepNumber + 1)
                .subject(previous != null ? previous.getSubject() : DEFAULT_SUBJECTS.getFirst())
                .bodyTemplate(previous != null ? previous.getBodyTemplate() : DEFAULT_BODY_TEMPLATE.getFirst())
                .attachmentIds(previous != null && previous.getAttachmentIds() != null
                        ? new ArrayList<>(previous.getAttachmentIds())
                        : null)
                .delayDays(4)
                .status(CampaignEmailStatus.PENDING)
                .build();

        return CampaignEmailDto.fromEntity(campaignEmailRepository.save(campaignEmail));
    }

    @Transactional
    public void deleteCampaignEmail(Long campaignId, Long emailId) {
        Campaign campaign = findCampaignById(campaignId);
        CampaignEmail campaignEmail = campaignEmailRepository
                .findByIdAndCampaignId(emailId, campaignId)
                .orElseThrow(() ->
                        new BadRequestException(
                                String.format("Campaign Email not found with id: %s for campaign: %s", emailId, campaignId)));

        if (Objects.equals(campaign.getStatus(), CampaignStatus.RUNNING)
                || Objects.equals(campaign.getStatus(), CampaignStatus.PAUSED)) {
            validateLiveDeletion(campaignId, campaignEmail);
            long count = campaignEmailRepository.countByCampaignId(campaignId);
            if (count <= 1) {
                throw new BadRequestException("Cannot delete all campaign emails");
            }
            deleteAndResyncCampaignEmail(campaign, campaignEmail);
            return;
        }

        long count = campaignEmailRepository.countByCampaignId(campaignId);
        if (count <= 1) {
            throw new BadRequestException("Cannot delete all campaign emails");
        }

        int deletedStepNumber = campaignEmail.getStepNumber();

        campaignEmailRepository.delete(campaignEmail);

        List<CampaignEmail> emailsToShift =
                campaignEmailRepository.findAllByCampaignIdAndStepNumberGreaterThan(
                        campaignId, deletedStepNumber
                );

        emailsToShift.forEach(email ->
                email.setStepNumber(email.getStepNumber() - 1)
        );

        campaignEmailRepository.saveAll(emailsToShift);
    }

    private void validateCampaignNotRunning(Long campaignId) {
        Campaign campaign = findCampaignById(campaignId);
        if (Objects.equals(campaign.getStatus(), CampaignStatus.RUNNING)
                || Objects.equals(campaign.getStatus(), CampaignStatus.PAUSED)) {
            throw new BadRequestException("Cannot change the campaign sequence while the campaign is running or paused.");
        }
    }

    private void validateLiveDeletion(Long campaignId, CampaignEmail campaignEmail) {
        if (!Objects.equals(campaignEmail.getStatus(), CampaignEmailStatus.PENDING)) {
            throw new BadRequestException("Only pending campaign steps can be deleted while the campaign is running or paused.");
        }
    }

    private void deleteAndResyncCampaignEmail(Campaign campaign, CampaignEmail campaignEmail) {
        int deletedStepNumber = campaignEmail.getStepNumber();

        campaignEmailRepository.delete(campaignEmail);
        campaignEmailRepository.flush();

        List<CampaignEmail> emailsToShift = campaignEmailRepository.findAllByCampaignIdAndStepNumberGreaterThan(
                campaign.getId(),
                deletedStepNumber
        );
        emailsToShift.forEach(email -> email.setStepNumber(email.getStepNumber() - 1));
        campaignEmailRepository.saveAllAndFlush(emailsToShift);

        resyncActiveCampaignContactsAfterDeletion(campaign, deletedStepNumber);
    }

    /**
     * Rebuilds the live schedule after a pending step deletion in a running campaign.
     * When a pending step is removed, later steps are shifted up and every active affected
     * contact is aligned to the new step order so the orchestrator continues from the correct
     * sequence position without moving a previously scheduled send earlier than its original
     * nextSendAt.
     * If no successor step remains for an affected contact, the contact is marked completed.
     */
    private void resyncActiveCampaignContactsAfterDeletion(Campaign campaign, int deletedStepNumber) {
        List<CampaignContact> affectedContacts = campaignContactRepository
                .findAllByCampaignIdAndStatusAndParticipatingTrue(campaign.getId(), CampaignContactStatus.ACTIVE)
                .stream()
                .filter(contact -> contact.getCurrentStep() >= deletedStepNumber)
                .toList();

        if (affectedContacts.isEmpty()) {
            log.info(
                    "No active campaign contacts needed resync after deleting step {} from campaign {}.",
                    deletedStepNumber,
                    campaign.getId()
            );
            return;
        }

        log.info(
                "Resyncing {} active campaign contacts after deleting step {} from campaign {}.",
                affectedContacts.size(),
                deletedStepNumber,
                campaign.getId()
        );

        Map<Integer, CampaignEmail> emailByStep = campaignEmailRepository.findAllByCampaignId(campaign.getId()).stream()
                .collect(Collectors.toMap(CampaignEmail::getStepNumber, email -> email));

        List<CampaignContact> contactsToResync = new ArrayList<>();

        for (CampaignContact contact : affectedContacts) {
            int originalCurrentStep = contact.getCurrentStep();
            int updatedCurrentStep = contact.getCurrentStep() > deletedStepNumber
                    ? contact.getCurrentStep() - 1
                    : contact.getCurrentStep();
            contact.setCurrentStep(updatedCurrentStep);

            CampaignEmail nextCampaignEmail = emailByStep.get(updatedCurrentStep);
            if (nextCampaignEmail == null) {
                contact.setStatus(CampaignContactStatus.COMPLETED);
                contact.setNextSendAt(null);
                continue;
            }

            if (originalCurrentStep == deletedStepNumber) {
                log.debug(
                        "Preserved campaign contact {} nextSendAt {} after deleting step {} from campaign {}.",
                        contact.getContactId(),
                        contact.getNextSendAt(),
                        deletedStepNumber,
                        campaign.getId()
                );
                continue;
            }

            contactsToResync.add(contact);
        }

        if (contactsToResync.isEmpty()) {
            campaignContactRepository.saveAllAndFlush(affectedContacts);
            log.info(
                    "Completed resync for {} active campaign contacts after deleting step {} from campaign {}.",
                    affectedContacts.size(),
                    deletedStepNumber,
                    campaign.getId()
            );
            return;
        }

        Map<Long, LeadContactDto> leadContactMap = leadContactService.getLeadContactsByIds(
                        contactsToResync.stream()
                                .map(CampaignContact::getContactId)
                                .toList()
                ).stream()
                .collect(Collectors.toMap(LeadContactDto::getId, leadContact -> leadContact));

        CampaignDto campaignDto = CampaignDto.fromEntity(campaign);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        for (CampaignContact contact : contactsToResync) {
            LeadContactDto leadContact = leadContactMap.get(contact.getContactId());
            CampaignEmail nextCampaignEmail = emailByStep.get(contact.getCurrentStep());
            LocalDateTime originalNextSendAt = contact.getNextSendAt();
            CampaignContactInfoDto contactInfo = CampaignContactInfoDto.builder()
                    .id(contact.getId())
                    .campaignId(contact.getCampaignId())
                    .contactId(contact.getContactId())
                    .currentStep(contact.getCurrentStep())
                    .status(contact.getStatus())
                    .locationState(leadContact != null ? leadContact.getLocationState() : null)
                    .locationCountry(leadContact != null ? leadContact.getLocationCountry() : null)
                    .build();
            LocalDateTime candidateNextSendAt = sendingWindowService.nextValidSendTime(
                    now.plusDays(nextCampaignEmail.getDelayDays()),
                    contactInfo,
                    campaignDto
            );
            if (originalNextSendAt != null && originalNextSendAt.isAfter(candidateNextSendAt)) {
                candidateNextSendAt = originalNextSendAt;
            }
            contact.setNextSendAt(candidateNextSendAt);
            log.debug(
                    "Resynced campaign contact {} in campaign {} to step {} with nextSendAt {} after deleting step {}.",
                    contact.getContactId(),
                    campaign.getId(),
                    contact.getCurrentStep(),
                    contact.getNextSendAt(),
                    deletedStepNumber
            );
        }
        campaignContactRepository.saveAllAndFlush(affectedContacts);
        log.info(
                "Completed resync for {} active campaign contacts after deleting step {} from campaign {}.",
                affectedContacts.size(),
                deletedStepNumber,
                campaign.getId()
        );
    }

    private void validateCampaignById(Long id) {
        campaignRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Campaign not found with id: " + id));
    }

    private Campaign findCampaignById(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
    }
}
