package ai.leadplus.application.workspaces;

import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.workspaces.Workspace;
import ai.leadplus.domain.workspaces.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private WorkspaceService workspaceService;

    @Test
    void createWorkspace_shouldReturnWorkspaceDto_whenValidInput() {
        WorkspaceDto inputDto = WorkspaceDto.builder().id(1L).name("Workspace Name").build();
        Workspace workspace = Workspace.builder().id(1L).name("Workspace Name").build();
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        WorkspaceDto result = workspaceService.createWorkspace(inputDto);

        assertEquals(1L, result.getId());
        assertEquals("Workspace Name", result.getName());
        verify(workspaceRepository).save(any(Workspace.class));
    }

    @Test
    void getWorkspaceById_shouldReturnWorkspaceDto_whenWorkspaceExists() {
        Workspace workspace = Workspace.builder().id(1L).name("Workspace Name").build();
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));

        WorkspaceDto result = workspaceService.getWorkspaceById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Workspace Name", result.getName());
        verify(workspaceRepository).findById(1L);
    }

    @Test
    void getWorkspaceById_shouldThrowResourceNotFoundException_whenWorkspaceDoesNotExist() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> workspaceService.getWorkspaceById(1L));
        verify(workspaceRepository).findById(1L);
    }

    @Test
    void updateWorkspace_shouldReturnUpdatedWorkspaceDto_whenWorkspaceExists() {
        Workspace existingWorkspace = Workspace.builder()
                .id(1L)
                .name("Old Name")
                .dailySendLimit(10)
                .ccRecipients(new ArrayList<>())
                .bccRecipients(new ArrayList<>())
                .build();

        WorkspaceDto updateDto = WorkspaceDto.builder()
                .name("New Name")
                .dailySendLimit(20)
                .ccRecipients(List.of(new RecipientDto("cc@example.com", "CC")))
                .bccRecipients(List.of(new RecipientDto("bcc@example.com", "BCC")))
                .build();

        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(existingWorkspace));
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(existingWorkspace);

        WorkspaceDto result = workspaceService.updateWorkspace(1L, updateDto);

        assertEquals(1L, result.getId());
        assertEquals("New Name", result.getName());
        assertEquals(20, result.getDailySendLimit());
        assertEquals(1, result.getCcRecipients().size());
        assertEquals("cc@example.com", result.getCcRecipients().getFirst().getEmail());
        assertEquals(1, result.getBccRecipients().size());
        assertEquals("bcc@example.com", result.getBccRecipients().getFirst().getEmail());

        verify(workspaceRepository).findById(1L);
        verify(workspaceRepository).save(any(Workspace.class));
    }

    @Test
    void updateWorkspace_shouldThrowResourceNotFoundException_whenWorkspaceDoesNotExist() {
        WorkspaceDto updateDto = WorkspaceDto.builder()
                .name("New Name")
                .dailySendLimit(200)
                .ccRecipients(List.of(new RecipientDto("cc@example.com", "CC")))
                .bccRecipients(List.of(new RecipientDto("bcc@example.com", "BCC")))
                .build();

        when(workspaceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> workspaceService.updateWorkspace(1L, updateDto));
        verify(workspaceRepository).findById(1L);
    }
}
