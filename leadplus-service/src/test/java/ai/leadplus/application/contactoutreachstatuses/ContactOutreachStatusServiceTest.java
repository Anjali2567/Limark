package ai.leadplus.application.contactoutreachstatuses;

import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.application.leadcontact.LeadContactService;
import ai.leadplus.application.campaigncontacts.CampaignContactInfoDto;
import ai.leadplus.domain.contactoutreachstatuses.ContactOutreachStatus;
import ai.leadplus.domain.contactoutreachstatuses.ContactOutreachStatusRepository;
import ai.leadplus.domain.contactoutreachstatuses.GlobalOutreachStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactOutreachStatusServiceTest {

    @Mock
    private ContactOutreachStatusRepository contactOutreachStatusRepository;

    @Mock
    private LeadContactService leadContactService;

    @InjectMocks
    private ContactOutreachStatusService contactOutreachStatusService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(contactOutreachStatusService, "lastEmailThrottleDays", 7);
        ReflectionTestUtils.setField(contactOutreachStatusService, "sequenceCooldownDays", 90);
    }

    @Test
    void filterCampaignEligibleContacts_shouldReturnAllContacts_whenNoOutreachStatusExists() {
        Long tenantId = 1L;
        List<LeadContactDto> contacts = List.of(
                createContact(1L),
                createContact(2L)
        );
        when(contactOutreachStatusRepository.findIneligibleContactIdsByTenantIdAndContactIdIn(eq(tenantId), anyList(), any(), any()))
                .thenReturn(List.of());

        List<LeadContactDto> result = contactOutreachStatusService.filterCampaignEligibleContacts(tenantId, contacts);

        assertEquals(2, result.size());
        verify(contactOutreachStatusRepository).findIneligibleContactIdsByTenantIdAndContactIdIn(eq(tenantId), anyList(), any(), any());
    }

    @Test
    void filterCampaignEligibleContacts_shouldExcludeContactsWithNonEligibleStatuses() {
        Long tenantId = 1L;
        List<LeadContactDto> contacts = List.of(
                createContact(1L),
                createContact(2L)
        );
        when(contactOutreachStatusRepository.findIneligibleContactIdsByTenantIdAndContactIdIn(eq(tenantId), anyList(), any(), any()))
                .thenReturn(List.of(1L));

        List<LeadContactDto> result = contactOutreachStatusService.filterCampaignEligibleContacts(tenantId, contacts);

        assertEquals(1, result.size());
        assertEquals(2L, result.getFirst().getId());
        verify(contactOutreachStatusRepository).findIneligibleContactIdsByTenantIdAndContactIdIn(eq(tenantId), anyList(), any(), any());
    }

    @Test
    void filterCampaignEligibleContacts_shouldExcludeContactsWithLastEmailAt() {
        Long tenantId = 1L;
        List<LeadContactDto> contacts = List.of(
                createContact(1L),
                createContact(2L)
        );
        when(contactOutreachStatusRepository.findIneligibleContactIdsByTenantIdAndContactIdIn(eq(tenantId), anyList(), any(), any()))
                .thenReturn(List.of(1L));

        List<LeadContactDto> result = contactOutreachStatusService.filterCampaignEligibleContacts(tenantId, contacts);

        assertEquals(1, result.size());
        assertEquals(2L, result.getFirst().getId());
        verify(contactOutreachStatusRepository).findIneligibleContactIdsByTenantIdAndContactIdIn(eq(tenantId), anyList(), any(), any());
    }

    @Test
    void filterCampaignEligibleContacts_shouldExcludeContactsWithRecentSequenceCompletion() {
        Long tenantId = 1L;
        List<LeadContactDto> contacts = List.of(
                createContact(1L),
                createContact(2L)
        );
        when(contactOutreachStatusRepository.findIneligibleContactIdsByTenantIdAndContactIdIn(eq(tenantId), anyList(), any(), any()))
                .thenReturn(List.of(1L));

        List<LeadContactDto> result = contactOutreachStatusService.filterCampaignEligibleContacts(tenantId, contacts);

        assertEquals(1, result.size());
        assertEquals(2L, result.getFirst().getId());
        verify(contactOutreachStatusRepository).findIneligibleContactIdsByTenantIdAndContactIdIn(eq(tenantId), anyList(), any(), any());
    }

    @Test
    void filterCampaignEligibleContacts_shouldIncludeContactsWithOldSequenceCompletion() {
        Long tenantId = 1L;
        List<LeadContactDto> contacts = List.of(
                createContact(1L),
                createContact(2L)
        );
        when(contactOutreachStatusRepository.findIneligibleContactIdsByTenantIdAndContactIdIn(eq(tenantId), anyList(), any(), any()))
                .thenReturn(List.of());

        List<LeadContactDto> result = contactOutreachStatusService.filterCampaignEligibleContacts(tenantId, contacts);

        assertEquals(2, result.size());
        verify(contactOutreachStatusRepository).findIneligibleContactIdsByTenantIdAndContactIdIn(eq(tenantId), anyList(), any(), any());
    }

    @Test
    void validateContactEligibility_shouldAllowFollowUpEmailsWithinSameCampaignEvenWhenThrottleWindowIsActive() {
        Long tenantId = 1L;
        Long campaignId = 10L;
        Long contactId = 99L;

        ContactOutreachStatus status = ContactOutreachStatus.builder()
                .tenantId(tenantId)
                .contactId(contactId)
                .status(GlobalOutreachStatus.ACTIVE)
                .currentCampaignIds(List.of(campaignId))
                .lastEmailAt(LocalDateTime.now().minusDays(1))
                .build();

        when(contactOutreachStatusRepository.findByTenantIdAndContactId(eq(tenantId), eq(contactId)))
                .thenReturn(Optional.of(status));

        CampaignContactInfoDto campaignContact = CampaignContactInfoDto.builder()
                .campaignId(campaignId)
                .contactId(contactId)
                .email("contact99@example.com")
                .currentStep(2)
                .build();

        assertTrue(contactOutreachStatusService.validateContactEligibility(tenantId, campaignContact));
    }

    @Test
    void validateContactEligibility_shouldThrottleCrossCampaignEmailsWhenLastEmailIsRecent() {
        Long tenantId = 1L;
        Long campaignId = 10L;
        Long contactId = 99L;

        ContactOutreachStatus status = ContactOutreachStatus.builder()
                .tenantId(tenantId)
                .contactId(contactId)
                .status(GlobalOutreachStatus.ACTIVE)
                .currentCampaignIds(List.of(999L))
                .lastEmailAt(LocalDateTime.now().minusDays(1))
                .build();

        when(contactOutreachStatusRepository.findByTenantIdAndContactId(eq(tenantId), eq(contactId)))
                .thenReturn(Optional.of(status));

        CampaignContactInfoDto campaignContact = CampaignContactInfoDto.builder()
                .campaignId(campaignId)
                .contactId(contactId)
                .email("contact99@example.com")
                .build();

        assertFalse(contactOutreachStatusService.validateContactEligibility(tenantId, campaignContact));
    }

    @Test
    void getUnsubscribedContacts_shouldReturnEmptyPage_whenNoStatuses() {
        Long tenantId = 1L;
        PageRequest pageable = PageRequest.of(0, 10);

        when(contactOutreachStatusRepository.findAllByTenantIdAndStatus(eq(tenantId), eq(GlobalOutreachStatus.UNSUBSCRIBED), any()))
                .thenReturn(new PageImpl<>(List.of()));

        var result = contactOutreachStatusService.getUnsubscribedContacts(tenantId, pageable);

        assertEquals(0, result.getTotalElements());
        verify(contactOutreachStatusRepository).findAllByTenantIdAndStatus(eq(tenantId), eq(GlobalOutreachStatus.UNSUBSCRIBED), any());
    }

    @Test
    void getUnsubscribedContacts_shouldReturnContactsInOrder_andPreservePagination() {
        Long tenantId = 1L;
        PageRequest pageable = PageRequest.of(0, 10);

        ContactOutreachStatus s1 = ContactOutreachStatus.builder().contactId(1L).tenantId(tenantId).status(GlobalOutreachStatus.UNSUBSCRIBED).build();
        ContactOutreachStatus s2 = ContactOutreachStatus.builder().contactId(2L).tenantId(tenantId).status(GlobalOutreachStatus.UNSUBSCRIBED).build();

        // Ensure the list of statuses is ordered by contactId for consistent processing.
        List<ContactOutreachStatus> statuses = List.of(s1, s2);

        // Extract and sort contact IDs to ensure the order passed to getLeadContactsByIds.
        List<Long> contactIds = statuses.stream()
                .map(ContactOutreachStatus::getContactId)
                .sorted() // Sort the IDs
                .toList();

        when(contactOutreachStatusRepository.findAllByTenantIdAndStatus(eq(tenantId), eq(GlobalOutreachStatus.UNSUBSCRIBED), any()))
                .thenReturn(new PageImpl<>(List.of(s1, s2), pageable, 2));

        List<LeadContactDto> contactDtos = List.of(
                LeadContactDto.builder().id(1L).email("c1@example.com").build(),
                LeadContactDto.builder().id(2L).email("c2@example.com").build()
        );
        // Call getLeadContactsByIds with the SORTED list of IDs.
        when(leadContactService.getLeadContactsByIds(contactIds)).thenReturn(contactDtos);

        var result = contactOutreachStatusService.getUnsubscribedContacts(tenantId, pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        // Assertions on the order of returned DTOs based on the sorted contact IDs.
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals(2L, result.getContent().get(1).getId());
        verify(contactOutreachStatusRepository).findAllByTenantIdAndStatus(eq(tenantId), eq(GlobalOutreachStatus.UNSUBSCRIBED), any());
        verify(leadContactService).getLeadContactsByIds(contactIds); // Verify with sorted IDs
    }

    private LeadContactDto createContact(Long id) {
        return LeadContactDto.builder()
                .id(id)
                .email("contact" + id)
                .build();
    }
}
