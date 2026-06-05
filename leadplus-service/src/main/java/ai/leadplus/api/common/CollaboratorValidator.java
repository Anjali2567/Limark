package ai.leadplus.api.common;

import ai.leadplus.application.collaborators.CollaboratorDto;
import ai.leadplus.application.collaborators.CollaboratorService;
import ai.leadplus.application.exception.UnauthorizedException;
import ai.leadplus.domain.collaborators.CollaboratorRole;
import ai.leadplus.domain.common.SourcingRequestType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollaboratorValidator {

    private final UserValidator userValidator;
    private final CollaboratorService collaboratorService;

    private final List<CollaboratorRole> editorRoles = List.of(
            CollaboratorRole.EDITOR,
            CollaboratorRole.OWNER
    );

    private final List<CollaboratorRole> viewerRoles = List.of(
            CollaboratorRole.EDITOR,
            CollaboratorRole.OWNER,
            CollaboratorRole.VIEWER,
            CollaboratorRole.BD
    );

    public void validateCollaborator(Long sourceId, SourcingRequestType type) {
        getAuthenticatedCollaborator(sourceId, type);
    }

    public CollaboratorDto getAuthenticatedCollaborator(Long sourceId, SourcingRequestType type) {
        String userId = userValidator.getAuthenticatedUserId();
        return collaboratorService.getCollaboratorBySourceIdAndUserId(sourceId, type, Long.valueOf(userId));
    }

    public void validateOwner(Long sourceId, SourcingRequestType type) {
        getValidatedUserIdBySourceIdAndRoles(sourceId, type, CollaboratorRole.OWNER);
    }

    public void validateEditor(Long sourceId, SourcingRequestType type) {
        getAuthenticatedEditor(sourceId, type);
    }

    public String getValidatedOwner(Long sourceId, SourcingRequestType type) {
        return getValidatedUserIdBySourceIdAndRoles(sourceId, type, CollaboratorRole.OWNER);
    }

    public String getAuthenticatedEditor(Long sourceId, SourcingRequestType type) {
        return getValidatedUserIdBySourceIdAndRoles(sourceId, type, editorRoles);
    }

    public void validateViewer(Long sourceId, SourcingRequestType type) {
        getValidatedUserIdBySourceIdAndRoles(sourceId, type, viewerRoles);
    }

    private String getValidatedUserIdBySourceIdAndRoles(Long sourceId, SourcingRequestType type, CollaboratorRole role) {
        return getValidatedUserIdBySourceIdAndRoles(sourceId, type, List.of(role));
    }

    private String getValidatedUserIdBySourceIdAndRoles(Long sourceId, SourcingRequestType type, List<CollaboratorRole> roles) {
        String userId = userValidator.getAuthenticatedUserId();
        CollaboratorDto collaboratorDto = collaboratorService.getCollaboratorBySourceIdAndUserId(sourceId, type, Long.valueOf(userId));
        if (!roles.contains(collaboratorDto.getRole())) {
            throw new UnauthorizedException("Unauthorized Access");
        }
        return userId;
    }
}
