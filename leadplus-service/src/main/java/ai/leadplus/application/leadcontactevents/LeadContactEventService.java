package ai.leadplus.application.leadcontactevents;

import ai.leadplus.application.campaignorchestrator.CampaignEmailRepliedEvent;
import ai.leadplus.application.campaignorchestrator.CampaignEmailSentEvent;
import ai.leadplus.application.campaigncontacts.CampaignContactDto;
import ai.leadplus.application.campaigncontacts.CampaignContactInfoDto;
import ai.leadplus.application.campaigncontacts.CampaignContactService;
import ai.leadplus.application.campaigns.CampaignDto;
import ai.leadplus.application.campaigns.CampaignLaunchedEvent;
import ai.leadplus.application.campaigns.CampaignService;
import ai.leadplus.application.contactemails.ContactEmailDto;
import ai.leadplus.application.contactemails.ContactEmailSentEvent;
import ai.leadplus.application.leadnotes.LeadNoteCreatedEvent;
import ai.leadplus.application.leadnotes.LeadNoteUpdatedEvent;
import ai.leadplus.application.leadnotes.LeadNoteDeletedEvent;
import ai.leadplus.application.leadnotes.LeadNoteDto;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.domain.common.LeadType;
import ai.leadplus.domain.leadcontactevents.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadContactEventService {

    private final LeadContactEventRepository leadContactEventRepository;
    private final CampaignService campaignService;
    private final CampaignContactService campaignContactService;
    private final UserService userService;

    private static final String SYSTEM_USER = "System";

    public Page<LeadContactEventDetailedDto> getByTenantWorkspaceAndContact(
            Long tenantId,
            Long workspaceId,
            Long contactId,
            LeadContactEventCategory category,
            Pageable pageable
    ) {
        Page<LeadContactEvent> page;
        if (category == null) {
            page = leadContactEventRepository.findAllByTenantIdAndWorkspaceIdAndContactId(
                    tenantId, workspaceId, contactId, pageable);
        } else {
            page = leadContactEventRepository.findAllByTenantIdAndWorkspaceIdAndContactIdAndCategory(
                    tenantId, workspaceId, contactId, category, pageable);
        }

        return mapEventsToDetailedDto(page);
    }

    public Page<LeadContactEventDetailedDto> getEventsByTenantIdAndContactId(
            Long tenantId,
            Long contactId,
            LeadContactEventCategory category,
            Pageable pageable
    ) {
        Page<LeadContactEvent> page;
        if (category == null) {
            page = leadContactEventRepository.findAllByTenantIdAndContactId(tenantId, contactId, pageable);
        } else {
            page = leadContactEventRepository.findAllByTenantIdAndContactIdAndCategory(tenantId, contactId, category, pageable);
        }

        return mapEventsToDetailedDto(page);
    }

    private Page<LeadContactEventDetailedDto> mapEventsToDetailedDto(Page<LeadContactEvent> page) {

        List<String> userIds = page.getContent().stream()
                .map(LeadContactEvent::getEventBy)
                .filter(Objects::nonNull)
                .filter(id -> !SYSTEM_USER.equalsIgnoreCase(id))
                .distinct()
                .toList();

        List<Long> parsedUserIds = userIds.stream()
                .filter(id -> !id.isBlank())
                .map(Long::parseLong)
                .toList();
        List<UserDto> users = CollectionUtils.isEmpty(parsedUserIds)
                ? List.of()
                : userService.getUsersByUserIds(parsedUserIds);

        Map<String, String> userMap = users.stream()
                .collect(Collectors.toMap(user -> String.valueOf(user.getId()), UserDto::getName));

        return page.map(event -> {

            String userId = event.getEventBy();
            String userName = null;

            if (StringUtils.hasText(userId)) {
                if (SYSTEM_USER.equalsIgnoreCase(userId)) {
                    userName = SYSTEM_USER;
                } else {
                    userName = userMap.get(userId);
                }
            }

            return LeadContactEventDetailedDto.fromEntity(event, userName);
        });
    }

    public List<LeadContactEventCountDto> getCountsByTenantWorkspaceAndContact(
            Long tenantId,
            Long workspaceId,
            Long contactId
    ) {
        List<CompletableFuture<LeadContactEventCountDto>> futures = Arrays.stream(LeadContactEventCategory.values())
                .map(category -> CompletableFuture.supplyAsync(() -> {
                    long count = leadContactEventRepository.countByTenantIdAndWorkspaceIdAndContactIdAndCategory(
                            tenantId, workspaceId, contactId, category);
                    return LeadContactEventCountDto.of(category, count);
                }))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    public List<LeadContactEventCountDto> getCountsByTenantAndContact(Long tenantId, Long contactId) {
        List<CompletableFuture<LeadContactEventCountDto>> futures = Arrays.stream(LeadContactEventCategory.values())
                .map(category -> CompletableFuture.supplyAsync(() -> {
                    long count = leadContactEventRepository.countByTenantIdAndContactIdAndCategory(
                            tenantId, contactId, category);
                    return LeadContactEventCountDto.of(category, count);
                }))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    @Async
    @EventListener
    public void handleCampaignEmailSentEvent(CampaignEmailSentEvent event) {
        CampaignDto campaignDto = event.getCampaignDto();
        CampaignContactInfoDto contactInfo = event.getCampaignContactInfoDto();
        LeadContactEvent leadContactEvent = LeadContactEvent.builder()
                .tenantId(event.getTenantId())
                .workspaceId(event.getWorkspaceId())
                .contactId(contactInfo.getContactId())
                .category(LeadContactEventCategory.CAMPAIGN)
                .type(LeadContactEventType.CAMPAIGN_EMAIL_SENT)
                .description("Campaign email sent with step: " + event.getCampaignEmailDto().getStepNumber())
                .sourceId(event.getCampaignEmailDto().getId())
                .sourceType(LeadContactEventSourceType.CAMPAIGN_EMAIL)
                .eventBy(campaignDto.getCreatedBy() != null ? String.valueOf(campaignDto.getCreatedBy()) : null)
                .eventAt(event.getSentAt())
                .active(true)
                .build();

        leadContactEventRepository.save(leadContactEvent);
    }

    @EventListener
    public void handleLeadNoteCreatedEvent(LeadNoteCreatedEvent event) {
        LeadNoteDto note = event.getLeadNoteDto();

        if (!Objects.equals(note.getType(), LeadType.LEAD_CONTACT)) {
            return;
        }

        LeadContactEvent leadContactEvent = LeadContactEvent.builder()
                .tenantId(note.getTenantId())
                .workspaceId(note.getWorkspaceId())
                .contactId(note.getSourceId())
                .category(LeadContactEventCategory.NOTE)
                .type(LeadContactEventType.NOTE_ADDED)
                .description("Note added to contact profile")
                .sourceId(note.getId())
                .sourceType(LeadContactEventSourceType.LEAD_NOTE)
                .eventBy(note.getCreatedBy() != null ? String.valueOf(note.getCreatedBy()) : null)
                .eventAt(note.getCreatedAt())
                .active(true)
                .build();

        leadContactEventRepository.save(leadContactEvent);
    }

    @EventListener
    public void handleLeadNoteUpdatedEvent(LeadNoteUpdatedEvent event) {
        LeadNoteDto note = event.getLeadNoteDto();

        if (!Objects.equals(note.getType(), LeadType.LEAD_CONTACT)) {
            return;
        }

        LeadContactEvent leadContactEvent = LeadContactEvent.builder()
                .tenantId(note.getTenantId())
                .workspaceId(note.getWorkspaceId())
                .contactId(note.getSourceId())
                .category(LeadContactEventCategory.NOTE)
                .type(LeadContactEventType.NOTE_UPDATED)
                .description("Note updated on contact profile")
                .sourceId(note.getId())
                .sourceType(LeadContactEventSourceType.LEAD_NOTE)
                .eventBy(note.getUpdatedBy() != null ? String.valueOf(note.getUpdatedBy()) : null)
                .eventAt(note.getUpdatedAt())
                .active(true)
                .build();

        leadContactEventRepository.save(leadContactEvent);
    }

    @EventListener
    public void handleLeadNoteDeletedEvent(LeadNoteDeletedEvent event) {
        LeadNoteDto note = event.getLeadNoteDto();

        if (!Objects.equals(note.getType(), LeadType.LEAD_CONTACT)) {
            return;
        }

        LeadContactEvent leadContactEvent = LeadContactEvent.builder()
                .tenantId(note.getTenantId())
                .workspaceId(note.getWorkspaceId())
                .contactId(note.getSourceId())
                .category(LeadContactEventCategory.NOTE)
                .type(LeadContactEventType.NOTE_DELETED)
                .description("Note deleted from contact profile")
                .sourceId(note.getId())
                .sourceType(LeadContactEventSourceType.LEAD_NOTE)
                .eventBy(note.getUpdatedBy() != null ? String.valueOf(note.getUpdatedBy()) : null)
                .eventAt(note.getUpdatedAt())
                .active(true)
                .build();

        leadContactEventRepository.save(leadContactEvent);
    }

    @Async
    @EventListener
    public void handleCampaignLaunchedEvent(CampaignLaunchedEvent event) {
        CampaignDto campaignDto = event.getCampaignDto();
        List<CampaignContactDto> campaignContacts = campaignContactService.getCampaignContactsByCampaignId(campaignDto.getId());
        if (CollectionUtils.isEmpty(campaignContacts))
            return;

        LocalDateTime eventAt = campaignDto.getLaunchedAt() != null ? campaignDto.getLaunchedAt() : LocalDateTime.now();

        List<LeadContactEvent> leadEvents = campaignContacts.stream()
                .map(contact -> LeadContactEvent.builder()
                        .tenantId(campaignDto.getTenantId())
                        .workspaceId(campaignDto.getWorkspaceId())
                        .contactId(contact.getContactId())
                        .category(LeadContactEventCategory.CAMPAIGN)
                        .type(LeadContactEventType.CAMPAIGN_INITIATED)
                        .description(campaignDto.getName())
                        .sourceId(campaignDto.getId())
                        .sourceType(LeadContactEventSourceType.CAMPAIGN)
                        .eventBy(campaignDto.getCreatedBy() != null ? String.valueOf(campaignDto.getCreatedBy()) : null)
                        .eventAt(eventAt)
                        .active(true)
                        .build())
                .toList();

        leadContactEventRepository.saveAll(leadEvents);
    }

    @Async
    @EventListener
    public void handleContactEmailSentEvent(ContactEmailSentEvent event) {
        ContactEmailDto contactEmail = event.getContactEmailDto();

        LeadContactEvent leadContactEvent = LeadContactEvent.builder()
                .tenantId(contactEmail.getTenantId())
                .workspaceId(contactEmail.getWorkspaceId())
                .contactId(contactEmail.getContactId())
                .category(LeadContactEventCategory.EMAIL)
                .type(LeadContactEventType.EMAIL_SENT)
                .description(contactEmail.getSubject())
                .sourceId(contactEmail.getId())
                .sourceType(LeadContactEventSourceType.CONTACT_EMAIL)
                .eventBy(contactEmail.getCreatedBy() != null ? String.valueOf(contactEmail.getCreatedBy()) : null)
                .eventAt(event.getSentAt())
                .active(true)
                .build();

        leadContactEventRepository.save(leadContactEvent);
    }

    @Async
    @EventListener
    public void handleCampaignEmailRepliedEvent(CampaignEmailRepliedEvent event) {
        CampaignContactDto campaignContactDto = event.getCampaignContactDto();
        CampaignDto campaignDto = campaignService.getCampaignById(campaignContactDto.getCampaignId());

        LeadContactEvent leadContactEvent = LeadContactEvent.builder()
                .tenantId(campaignDto.getTenantId())
                .workspaceId(campaignDto.getWorkspaceId())
                .contactId(campaignContactDto.getContactId())
                .category(LeadContactEventCategory.CAMPAIGN)
                .type(LeadContactEventType.CAMPAIGN_EMAIL_REPLIED)
                .description("Campaign email replied")
                .sourceId(campaignContactDto.getId())
                .sourceType(LeadContactEventSourceType.CAMPAIGN_CONTACT)
                .eventBy(SYSTEM_USER)
                .eventAt(event.getRepliedAt())
                .active(true)
                .build();

        leadContactEventRepository.save(leadContactEvent);
    }
}
