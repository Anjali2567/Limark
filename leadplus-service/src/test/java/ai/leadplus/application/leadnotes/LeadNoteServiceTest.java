package ai.leadplus.application.leadnotes;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.leadcontact.LeadContactService;
import ai.leadplus.domain.common.LeadType;
import ai.leadplus.domain.leadnotes.LeadNote;
import ai.leadplus.domain.leadnotes.LeadNoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadNoteServiceTest {

    @Mock
    private LeadNoteRepository leadNoteRepository;

    @Mock
    private LeadContactService leadContactService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LeadNoteService leadNoteService;

    private LeadNote leadNote;
    private LeadNoteDto leadNoteDto;

    private final Long ID = 1L;
    private final Long TENANT = 1L;
    private final Long WORKSPACE = 1L;
    private final Long SOURCE_ID = 1L;

    @BeforeEach
    void setUp() {
        leadNote = LeadNote.builder()
                .id(ID)
                .tenantId(TENANT)
                .workspaceId(WORKSPACE)
                .note("Test Note")
                .type(LeadType.LEAD_CONTACT)
                .sourceId(SOURCE_ID)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        leadNoteDto = LeadNoteDto.builder()
                .note("Test Note")
                .type(LeadType.LEAD_CONTACT)
                .sourceId(SOURCE_ID)
                .build();
    }

    @Test
    void createLeadNote_shouldCreateSuccessfully() {
        when(leadNoteRepository.save(any(LeadNote.class))).thenReturn(leadNote);

        LeadNoteDto result = leadNoteService.createLeadNote(leadNoteDto, TENANT, WORKSPACE);

        assertThat(result).isNotNull();
        assertThat(result.getTenantId()).isEqualTo(TENANT);
        assertThat(result.getWorkspaceId()).isEqualTo(WORKSPACE);

        verify(leadContactService).validateContactId(SOURCE_ID);
        verify(leadNoteRepository).save(any(LeadNote.class));
    }

    @Test
    void getById_shouldReturnLeadNote() {
        when(leadNoteRepository
                .findByIdAndTenantIdAndWorkspaceIdAndActiveTrue(ID, TENANT, WORKSPACE))
                .thenReturn(Optional.of(leadNote));

        LeadNoteDto result = leadNoteService.getById(ID, TENANT, WORKSPACE);

        assertThat(result.getId()).isEqualTo(ID);
    }

    @Test
    void getById_shouldThrowNotFound_whenNotExists() {
        when(leadNoteRepository
                .findByIdAndTenantIdAndWorkspaceIdAndActiveTrue(ID, TENANT, WORKSPACE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                leadNoteService.getById(ID, TENANT, WORKSPACE))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getLeadNotes_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<LeadNote> page = new PageImpl<>(List.of(leadNote));

        when(leadNoteRepository
                .findAllByTenantIdAndWorkspaceIdAndSourceIdAndActiveTrue(TENANT, WORKSPACE, SOURCE_ID, pageable))
                .thenReturn(page);

        Page<LeadNoteDto> result =
                leadNoteService.getLeadNotes(TENANT, WORKSPACE, SOURCE_ID, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(ID);
    }

    @Test
    void updateLeadNote_shouldUpdateSuccessfully() {
        when(leadNoteRepository
                .findByIdAndTenantIdAndWorkspaceIdAndActiveTrue(ID, TENANT, WORKSPACE))
                .thenReturn(Optional.of(leadNote));

        when(leadNoteRepository.save(any(LeadNote.class))).thenReturn(leadNote);

        LeadNoteDto result =
                leadNoteService.updateLeadNote("Updated Note", ID, TENANT, WORKSPACE);

        assertThat(result.getNote()).isEqualTo("Updated Note");

        verify(leadNoteRepository).save(leadNote);
    }

    @Test
    void deleteLeadNote_shouldSoftDelete() {
        when(leadNoteRepository
                .findByIdAndTenantIdAndWorkspaceIdAndActiveTrue(ID, TENANT, WORKSPACE))
                .thenReturn(Optional.of(leadNote));

        when(leadNoteRepository.save(any())).thenReturn(leadNote);

        leadNoteService.deleteLeadNote(ID, TENANT, WORKSPACE);

        assertThat(leadNote.isActive()).isFalse();
        verify(leadNoteRepository).save(leadNote);
    }
}
