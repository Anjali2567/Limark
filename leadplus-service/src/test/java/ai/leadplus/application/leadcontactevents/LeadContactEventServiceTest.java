package ai.leadplus.application.leadcontactevents;

import ai.leadplus.application.leadnotes.*;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.domain.common.LeadType;
import ai.leadplus.domain.leadcontactevents.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadContactEventServiceTest {

    @Mock
    private LeadContactEventRepository leadContactEventRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private LeadContactEventService leadContactEventService;

    private final Long TENANT = 1L;
    private final Long WORKSPACE = 2L;
    private final Long CONTACT = 3L;

    private LeadContactEvent userEvent;
    private LeadContactEvent systemEvent;

    @BeforeEach
    void setUp() {
        userEvent = LeadContactEvent.builder()
                .id(1L)
                .tenantId(TENANT)
                .workspaceId(WORKSPACE)
                .contactId(CONTACT)
                .category(LeadContactEventCategory.EMAIL)
                .type(LeadContactEventType.EMAIL_SENT)
                .description("User sent email")
                .sourceId(5L)
                .sourceType(LeadContactEventSourceType.CONTACT_EMAIL)
                .eventBy("1")
                .eventAt(LocalDateTime.now())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        systemEvent = LeadContactEvent.builder()
                .id(2L)
                .tenantId(TENANT)
                .workspaceId(WORKSPACE)
                .contactId(CONTACT)
                .category(LeadContactEventCategory.CAMPAIGN)
                .type(LeadContactEventType.CAMPAIGN_INITIATED)
                .description("Campaign started")
                .sourceId(5L)
                .sourceType(LeadContactEventSourceType.CAMPAIGN)
                .eventBy("System")
                .eventAt(LocalDateTime.now())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getByTenantWorkspaceAndContact_shouldReturnDetailedPage_withUserNames() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<LeadContactEvent> page = new PageImpl<>(List.of(userEvent, systemEvent));

        when(leadContactEventRepository.findAllByTenantIdAndWorkspaceIdAndContactId(TENANT, WORKSPACE, CONTACT, pageable))
                .thenReturn(page);
        when(userService.getUsersByUserIds(List.of(1L)))
                .thenReturn(List.of(UserDto.builder().id(1L).name("Alice").build()));

        Page<LeadContactEventDetailedDto> result = leadContactEventService.getByTenantWorkspaceAndContact(
                TENANT, WORKSPACE, CONTACT, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().stream().map(LeadContactEventDetailedDto::getUserName))
                .containsExactlyInAnyOrder("Alice", "System");

        verify(leadContactEventRepository).findAllByTenantIdAndWorkspaceIdAndContactId(TENANT, WORKSPACE, CONTACT, pageable);
        verify(userService).getUsersByUserIds(List.of(1L));
    }

    @Test
    void getEventsByTenantIdAndContactId_shouldReturnDetailedPage_withUserNames() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<LeadContactEvent> page = new PageImpl<>(List.of(userEvent, systemEvent));

        when(leadContactEventRepository.findAllByTenantIdAndContactId(TENANT, CONTACT, pageable))
                .thenReturn(page);
        when(userService.getUsersByUserIds(List.of(1L)))
                .thenReturn(List.of(UserDto.builder().id(1L).name("Alice").build()));

        Page<LeadContactEventDetailedDto> result = leadContactEventService.getEventsByTenantIdAndContactId(
                TENANT, CONTACT, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().stream().map(LeadContactEventDetailedDto::getUserName))
                .containsExactlyInAnyOrder("Alice", "System");

        verify(leadContactEventRepository).findAllByTenantIdAndContactId(TENANT, CONTACT, pageable);
        verify(userService).getUsersByUserIds(List.of(1L));
    }

    @Test
    void handleLeadNoteCreatedEvent_shouldCreateLeadContactEvent_forContactNote() {
        LeadNoteDto noteDto = LeadNoteDto.builder()
                .id(1L)
                .tenantId(TENANT)
                .workspaceId(WORKSPACE)
                .type(LeadType.LEAD_CONTACT)
                .sourceId(CONTACT)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();

        leadContactEventService.handleLeadNoteCreatedEvent(new LeadNoteCreatedEvent(this, noteDto));

        ArgumentCaptor<LeadContactEvent> captor = ArgumentCaptor.forClass(LeadContactEvent.class);
        verify(leadContactEventRepository).save(captor.capture());
        LeadContactEvent saved = captor.getValue();

        assertThat(saved).isNotNull();
        assertThat(saved.getTenantId()).isEqualTo(TENANT);
        assertThat(saved.getWorkspaceId()).isEqualTo(WORKSPACE);
        assertThat(saved.getContactId()).isEqualTo(CONTACT);
        assertThat(saved.getSourceId()).isEqualTo(1L);
        assertThat(saved.getCategory()).isEqualTo(LeadContactEventCategory.NOTE);
        assertThat(saved.getType()).isEqualTo(LeadContactEventType.NOTE_ADDED);
        assertThat(saved.getDescription()).isEqualTo("Note added to contact profile");
        assertThat(saved.getEventBy()).isEqualTo("1");
        assertThat(saved.isActive()).isTrue();
    }
}
