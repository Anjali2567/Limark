package ai.leadplus.application.workspaces;

import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.workspaces.Workspace;
import ai.leadplus.domain.workspaces.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    @Value("${workspace.max-per-user}")
    private int maxWorkspacesPerUser;

    @Value("${mailbox.daily-send-limit}")
    private int dailySendLimit = 30;

    public WorkspaceDto createWorkspace(WorkspaceDto workspaceDto) {
        Workspace workspace = workspaceDto.toEntity();
        workspace.setDailySendLimit(workspaceDto.getDailySendLimit() > 0 ? workspaceDto.getDailySendLimit() : dailySendLimit);
        workspaceRepository.save(workspace);
        return WorkspaceDto.fromEntity(workspace);
    }

    public void saveWorkspace(WorkspaceDto workspaceDto) {
        Workspace workspace = workspaceDto.toEntity();
        workspaceRepository.save(workspace);
    }

    public WorkspaceDto getWorkspaceById(Long id) {
        Workspace workspace = findWorkspaceById(id);
        return WorkspaceDto.fromEntity(workspace);
    }

    public WorkspaceDto updateWorkspace(Long id, WorkspaceDto workspaceDto) {
        Workspace workspace = findWorkspaceById(id);

        if (workspaceDto.getDailySendLimit() < 1 || workspaceDto.getDailySendLimit() > 50) {
            throw new BadRequestException("Daily send limit must be between 1 and 50");
        }

        workspace.setName(workspaceDto.getName());
        workspace.setDailySendLimit(workspaceDto.getDailySendLimit());
        workspace.setCcRecipients(workspaceDto.getCcRecipients().stream().map(RecipientDto::toEntity).toList());
        workspace.setBccRecipients(workspaceDto.getBccRecipients().stream().map(RecipientDto::toEntity).toList());

        return WorkspaceDto.fromEntity(workspaceRepository.save(workspace));
    }

    private Workspace findWorkspaceById(Long id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + id));
    }

    public Map<Long, WorkspaceDto> getAllWorkspacesByIds(Set<Long> workspaceIds) {
        return workspaceRepository.findAllById(workspaceIds)
                .stream()
                .map(WorkspaceDto::fromEntity)
                .collect(Collectors.toMap(WorkspaceDto::getId, Function.identity()));
    }

    public void validateWorkspaceCreationLimit(Long userId) {
        int workspaceCount = workspaceRepository.countWorkspacesByOwnerId(userId);

        if (workspaceCount >= maxWorkspacesPerUser) {
            throw new BadRequestException(
                    "You have reached the maximum number of workspaces you can own (" + maxWorkspacesPerUser + ")"
            );
        }
    }

    public Page<WorkspaceDto> getAllWorkspacesByTenantId(Long tenantId, String query, Pageable pageable) {
        Page<Workspace> workspacePage;
        if (StringUtils.hasText(query)) {
            workspacePage = workspaceRepository.findByTenantIdAndNameContainingIgnoreCase(tenantId, query, pageable);
        } else {
            workspacePage = workspaceRepository.findByTenantId(tenantId, pageable);
        }
        return workspacePage.map(WorkspaceDto::fromEntity);
    }
}
