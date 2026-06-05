package ai.leadplus.api.v1.requestforquotes;

import ai.leadplus.api.common.UserValidator;
import ai.leadplus.api.v1.common.collaborators.CollaboratorInviteRequest;
import ai.leadplus.api.v1.common.collaborators.CollaboratorRequest;
import ai.leadplus.api.v1.common.collaborators.CollaboratorResponse;
import ai.leadplus.api.v1.common.collaborators.CollaboratorUserResponse;
import ai.leadplus.application.collaborators.CollaboratorDto;
import ai.leadplus.application.collaborators.CollaboratorService;
import ai.leadplus.application.collaborators.CollaboratorUserDto;
import ai.leadplus.domain.common.SourcingRequestType;
import ai.leadplus.api.common.CollaboratorValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/v1/request-for-quotes/{requestForQuoteId}/collaborators")
@RequiredArgsConstructor
@Tag(name = "Request For Quote Collaborators", description = "Manage collaborators on a Request For Quote")
public class RequestForQuoteCollaboratorController {

    private final CollaboratorService collaboratorService;
    private final UserValidator userValidator;
    private final CollaboratorValidator collaboratorValidator;

    @Operation(summary = "Get all collaborators for a Request For Quote")
    @GetMapping
    public ResponseEntity<List<CollaboratorUserResponse>> getAllCollaborators(
            @PathVariable Long requestForQuoteId) {
        collaboratorValidator.validateViewer(requestForQuoteId, SourcingRequestType.REQUEST_FOR_QUOTE);
        log.info("[GET] Get all collaborators for RFQ {}", requestForQuoteId);
        List<CollaboratorUserDto> dtoList = collaboratorService
                .getAllCollaboratorsBySourceId(requestForQuoteId, SourcingRequestType.REQUEST_FOR_QUOTE);
        List<CollaboratorUserResponse> response = dtoList.stream()
                .map(CollaboratorUserResponse::toResponse)
                .collect(Collectors.toList());
        log.info("[GET] Retrieved {} collaborators for RFQ {}", response.size(), requestForQuoteId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get the authenticated user's collaborator record for a Request For Quote")
    @GetMapping("/user")
    public ResponseEntity<CollaboratorResponse> getCollaboratorByUserId(
            @PathVariable Long requestForQuoteId) {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[GET] Get collaborator for RFQ {} and userId {}", requestForQuoteId, userId);
        CollaboratorDto dto = collaboratorService
                .getCollaboratorBySourceIdAndUserId(requestForQuoteId, SourcingRequestType.REQUEST_FOR_QUOTE, Long.valueOf(userId));
        log.info("[GET] Retrieved collaborator for RFQ {}", requestForQuoteId);
        return ResponseEntity.ok(CollaboratorResponse.toResponse(dto));
    }

    @Operation(summary = "Add a collaborator to a Request For Quote")
    @PostMapping
    public ResponseEntity<CollaboratorUserResponse> addCollaborator(
            @PathVariable Long requestForQuoteId,
            @RequestBody @Valid CollaboratorInviteRequest request) {
        String ownerUserId = collaboratorValidator.getValidatedOwner(requestForQuoteId, SourcingRequestType.REQUEST_FOR_QUOTE);
        log.info("[POST] Add collaborator to RFQ {}", requestForQuoteId);
        CollaboratorUserDto dto = collaboratorService.addCollaborator(
                requestForQuoteId,
                SourcingRequestType.REQUEST_FOR_QUOTE,
                request.getEmail(),
                request.getRole(),
                ownerUserId
        );
        CollaboratorUserResponse response = CollaboratorUserResponse.toResponse(dto);
        log.info("[POST] Added collaborator {} to RFQ {}", response.getUserId(), requestForQuoteId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update a collaborator's role on a Request For Quote")
    @PutMapping
    public ResponseEntity<CollaboratorResponse> updateCollaboratorRole(
            @PathVariable Long requestForQuoteId,
            @RequestBody @Valid CollaboratorRequest request) {
        String ownerUserId = collaboratorValidator.getValidatedOwner(requestForQuoteId, SourcingRequestType.REQUEST_FOR_QUOTE);
        CollaboratorDto dto = collaboratorService.updateCollaboratorRole(
                requestForQuoteId,
                SourcingRequestType.REQUEST_FOR_QUOTE,
                request.getUserId(),
                request.getRole(),
                Long.valueOf(ownerUserId)
        );
        CollaboratorResponse response = CollaboratorResponse.toResponse(dto);
        log.info("[PUT] Updated collaborator {} role to {} on RFQ {}", response.getUserId(), request.getRole(), requestForQuoteId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove a collaborator from a Request For Quote")
    @DeleteMapping
    public ResponseEntity<Void> deleteCollaborator(
            @PathVariable Long requestForQuoteId,
            @RequestParam Long userId) {
        collaboratorValidator.validateOwner(requestForQuoteId, SourcingRequestType.REQUEST_FOR_QUOTE);
        log.info("[DELETE] Remove collaborator {} from RFQ {}", userId, requestForQuoteId);
        collaboratorService.deleteCollaborator(requestForQuoteId, SourcingRequestType.REQUEST_FOR_QUOTE, userId);
        log.info("[DELETE] Removed collaborator {} from RFQ {}", userId, requestForQuoteId);
        return ResponseEntity.noContent().build();
    }
}
