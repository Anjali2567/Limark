package ai.leadplus.api.v1.requestforproposal;

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
@RequestMapping("/v1/request-for-proposals/{requestForProposalId}/collaborators")
@RequiredArgsConstructor
@Tag(name = "Request For Proposal Collaborators", description = "Manage collaborators on a Request For Proposal")
public class RequestForProposalCollaboratorController {

    private final CollaboratorService collaboratorService;
    private final UserValidator userValidator;
    private final CollaboratorValidator collaboratorValidator;

    @Operation(summary = "Get all collaborators for a Request For Proposal")
    @GetMapping
    public ResponseEntity<List<CollaboratorUserResponse>> getAllCollaborators(
            @PathVariable Long requestForProposalId) {
        collaboratorValidator.validateViewer(requestForProposalId, SourcingRequestType.REQUEST_FOR_PROPOSAL);
        log.info("[GET] Get all collaborators for RFP {}", requestForProposalId);
        List<CollaboratorUserDto> dtoList = collaboratorService
                .getAllCollaboratorsBySourceId(requestForProposalId, SourcingRequestType.REQUEST_FOR_PROPOSAL);
        List<CollaboratorUserResponse> response = dtoList.stream()
                .map(CollaboratorUserResponse::toResponse)
                .collect(Collectors.toList());
        log.info("[GET] Retrieved {} collaborators for RFP {}", response.size(), requestForProposalId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get the authenticated user's collaborator record for a Request For Proposal")
    @GetMapping("/user")
    public ResponseEntity<CollaboratorResponse> getCollaboratorByUserId(
            @PathVariable Long requestForProposalId) {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[GET] Get collaborator for RFP {} and userId {}", requestForProposalId, userId);
        CollaboratorDto dto = collaboratorService
                .getCollaboratorBySourceIdAndUserId(requestForProposalId, SourcingRequestType.REQUEST_FOR_PROPOSAL, Long.valueOf(userId));
        log.info("[GET] Retrieved collaborator for RFP {}", requestForProposalId);
        return ResponseEntity.ok(CollaboratorResponse.toResponse(dto));
    }

    @Operation(summary = "Add a collaborator to a Request For Proposal")
    @PostMapping
    public ResponseEntity<CollaboratorUserResponse> addCollaborator(
            @PathVariable Long requestForProposalId,
            @RequestBody @Valid CollaboratorInviteRequest request) {
        String ownerUserId = collaboratorValidator.getValidatedOwner(requestForProposalId, SourcingRequestType.REQUEST_FOR_PROPOSAL);
        log.info("[POST] Add collaborator to RFP {}", requestForProposalId);
        CollaboratorUserDto dto = collaboratorService.addCollaborator(
                requestForProposalId,
                SourcingRequestType.REQUEST_FOR_PROPOSAL,
                request.getEmail(),
                request.getRole(),
                ownerUserId
        );
        CollaboratorUserResponse response = CollaboratorUserResponse.toResponse(dto);
        log.info("[POST] Added collaborator {} to RFP {}", response.getUserId(), requestForProposalId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update a collaborator's role on a Request For Proposal")
    @PutMapping
    public ResponseEntity<CollaboratorResponse> updateCollaboratorRole(
            @PathVariable Long requestForProposalId,
            @RequestBody @Valid CollaboratorRequest request) {
        String ownerUserId = collaboratorValidator.getValidatedOwner(requestForProposalId, SourcingRequestType.REQUEST_FOR_PROPOSAL);
        log.info("[PUT] Update collaborator role on RFP {}", requestForProposalId);
        CollaboratorDto dto = collaboratorService.updateCollaboratorRole(
                requestForProposalId,
                SourcingRequestType.REQUEST_FOR_PROPOSAL,
                request.getUserId(),
                request.getRole(),
                Long.valueOf(ownerUserId)
        );
        CollaboratorResponse response = CollaboratorResponse.toResponse(dto);
        log.info("[PUT] Updated collaborator {} role to {} on RFP {}", response.getUserId(), request.getRole(), requestForProposalId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove a collaborator from a Request For Proposal")
    @DeleteMapping
    public ResponseEntity<Void> deleteCollaborator(
            @PathVariable Long requestForProposalId,
            @RequestParam Long userId) {
        collaboratorValidator.validateOwner(requestForProposalId, SourcingRequestType.REQUEST_FOR_PROPOSAL);
        log.info("[DELETE] Remove collaborator {} from RFP {}", userId, requestForProposalId);
        collaboratorService.deleteCollaborator(requestForProposalId, SourcingRequestType.REQUEST_FOR_PROPOSAL, userId);
        log.info("[DELETE] Removed collaborator {} from RFP {}", userId, requestForProposalId);
        return ResponseEntity.noContent().build();
    }
}
