package ai.leadplus.api.v1.leadnotes;

import ai.leadplus.application.leadnotes.LeadNoteDto;
import ai.leadplus.application.leadnotes.LeadNoteService;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/v1/tenants/{tenantId}/workspaces/{workspaceId}/lead-notes")
@RequiredArgsConstructor
@Tag(name = "Lead Notes", description = "Lead Note APIs")
public class LeadNoteController {

    private final LeadNoteService leadNoteService;
    private final UserService userService;

    @Operation(summary = "Create Lead Note")
    @PostMapping
    public ResponseEntity<LeadNoteResponse> createLeadNote(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @Valid @RequestBody LeadNoteRequest request) {
        log.info("[POST] Create Lead Note request received for tenantId={}, workspaceId={}", tenantId, workspaceId);
        LeadNoteDto saved = leadNoteService.createLeadNote(request.toDto(), tenantId, workspaceId);
        LeadNoteResponse response = LeadNoteResponse.fromDto(saved);
        log.info("[POST] Lead Note created with id={}", saved.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Lead Note by ID")
    @GetMapping("/{id}")
    public ResponseEntity<LeadNoteResponse> getById(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @PathVariable Long id) {

        log.info("[GET] Get Lead Note by id={} for tenantId={}, workspaceId={}", id, tenantId, workspaceId);
        LeadNoteDto dto = leadNoteService.getById(id, tenantId, workspaceId);
        LeadNoteResponse response = LeadNoteResponse.fromDto(dto);
        log.info("[GET] Lead Note retrieved with id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get All Lead Notes By SourceId")
    @GetMapping("sources/{sourceId}")
    public ResponseEntity<Page<LeadNoteResponse>> searchLeadNotes(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @PathVariable Long sourceId,
            Pageable pageable) {

        log.info("[GET] Get all Lead Notes");
        Page<LeadNoteDto> leadNoteDtos = leadNoteService.getLeadNotes(tenantId, workspaceId, sourceId, pageable);
        Map<Long, UserDto> userMap = getCreatedUsers(leadNoteDtos);

        Page<LeadNoteResponse> responses = leadNoteDtos.map(leadNote -> {
            UserDto creatorDto = userMap.getOrDefault(Long.valueOf(leadNote.getCreatedBy()), null);
            return LeadNoteResponse.fromDto(leadNote, creatorDto);
        });

        log.info("[GET] Retrieved {} Lead Notes", responses.getSize());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Update Lead Note")
    @PutMapping("/{id}")
    public ResponseEntity<LeadNoteResponse> updateLeadNote(
            @PathVariable Long id,
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @Valid @RequestBody LeadNoteUpdateRequest leadNoteUpdateRequest) {

        log.info("[PUT] Update Lead Note request received for tenantId={}, workspaceId={}", tenantId, workspaceId);
        LeadNoteDto saved = leadNoteService.updateLeadNote(leadNoteUpdateRequest.getNote(), id, tenantId, workspaceId);
        LeadNoteResponse response = LeadNoteResponse.fromDto(saved);
        log.info("[PUT] Lead Note Updated with id={}", saved.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Lead Note by ID (Soft Delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @PathVariable Long id) {

        log.info("[DELETE] Delete Lead Note by id={} tenantId={} workspaceId={}", id, tenantId, workspaceId);
        leadNoteService.deleteLeadNote(id, tenantId, workspaceId);
        log.info("[DELETE] Lead Note soft deleted id={}", id);
        return ResponseEntity.noContent().build();
    }

    private Map<Long, UserDto> getCreatedUsers(Page<LeadNoteDto> leadNoteDtos) {
        List<Long> createdByUserIds = leadNoteDtos.getContent().stream()
                .map(LeadNoteDto::getCreatedBy)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<UserDto> users = userService.getUsersByUserIds(createdByUserIds);

        log.info("[GET] Fetched details for {} unique users.", users.size());
        return users.stream().collect(Collectors.toMap(UserDto::getId, user -> user));
    }
}
