package ai.leadplus.application.leadnotes;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.leadcompany.LeadCompanyService;
import ai.leadplus.application.leadcontact.LeadContactService;
import ai.leadplus.domain.common.LeadType;
import ai.leadplus.domain.leadnotes.LeadNote;
import ai.leadplus.domain.leadnotes.LeadNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeadNoteService {

    private final LeadNoteRepository leadNoteRepository;
    private final LeadContactService leadContactService;
    private final LeadCompanyService leadCompanyService;
    private final ApplicationEventPublisher eventPublisher;

    public LeadNoteDto createLeadNote(LeadNoteDto dto, Long tenantId, Long workspaceId) {
        validateContactSourceId(dto.getSourceId(), dto.getType());

        dto.setTenantId(tenantId);
        dto.setWorkspaceId(workspaceId);
        dto.setActive(true);
        LeadNote saved = leadNoteRepository.save(dto.toEntity());
        LeadNoteDto savedDto = LeadNoteDto.fromEntity(saved);
        eventPublisher.publishEvent(new LeadNoteCreatedEvent(this, savedDto));
        return savedDto;
    }

    public LeadNoteDto getById(Long id, Long tenantId, Long workspaceId) {
        LeadNote leadNote = findById(id, tenantId, workspaceId);
        return LeadNoteDto.fromEntity(leadNote);
    }

    public Page<LeadNoteDto> getLeadNotes(Long tenantId, Long workspaceId, Long sourceId, Pageable pageable) {
        Page<LeadNote> notes = leadNoteRepository.findAllByTenantIdAndWorkspaceIdAndSourceIdAndActiveTrue(tenantId, workspaceId, sourceId, pageable);
        return notes.map(LeadNoteDto::fromEntity);
    }

    public LeadNoteDto updateLeadNote(String note, Long id, Long tenantId, Long workspaceId) {
        LeadNote existing = findById(id, tenantId, workspaceId);
        existing.setNote(note);
        LeadNote saved = leadNoteRepository.save(existing);
        LeadNoteDto savedDto = LeadNoteDto.fromEntity(saved);
        eventPublisher.publishEvent(new LeadNoteUpdatedEvent(this, savedDto));
        return savedDto;
    }

    public void deleteLeadNote(Long id, Long tenantId, Long workspaceId) {
        LeadNote existing = findById(id, tenantId, workspaceId);
        existing.setActive(false);
        LeadNote saved = leadNoteRepository.save(existing);
        LeadNoteDto dto = LeadNoteDto.fromEntity(saved);
        eventPublisher.publishEvent(new LeadNoteDeletedEvent(this, dto));
    }

    private LeadNote findById(Long id, Long tenantId, Long workspaceId) {
        return leadNoteRepository.findByIdAndTenantIdAndWorkspaceIdAndActiveTrue(id, tenantId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead Note not found with id: " + id));
    }

    private void validateContactSourceId(Long sourceId, LeadType type) {
        if (LeadType.LEAD_COMPANY.equals(type)) {
            leadCompanyService.validateCompanyId(sourceId);
        } else if (LeadType.LEAD_CONTACT.equals(type)) {
            leadContactService.validateContactId(sourceId);
        }
    }
}
