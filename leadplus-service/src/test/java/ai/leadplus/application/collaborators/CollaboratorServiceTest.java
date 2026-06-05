package ai.leadplus.application.collaborators;

import ai.leadplus.application.aws.EmailService;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.DuplicateResourceException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.domain.collaborators.Collaborator;
import ai.leadplus.domain.collaborators.CollaboratorRepository;
import ai.leadplus.domain.collaborators.CollaboratorRole;
import ai.leadplus.domain.common.SourcingRequestType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollaboratorServiceTest {

    @Mock
    private CollaboratorRepository collaboratorRepository;

    @Mock
    private UserService userService;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CollaboratorService collaboratorService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(collaboratorService, "clientUrl", "http://localhost:3000");
    }

    @Test
    void getAllCollaboratorsBySourceId_mapsToUserDtos() {
        Collaborator c = Collaborator.builder()
                .id(1L)
                .sourceId(1L)
                .sourceType(SourcingRequestType.REQUEST_FOR_QUOTE)
                .userId(1L)
                .role(CollaboratorRole.EDITOR)
                .active(true)
                .build();

        when(collaboratorRepository.findAllBySourceIdAndSourceTypeAndActiveTrue(1L, SourcingRequestType.REQUEST_FOR_QUOTE))
                .thenReturn(List.of(c));

        UserDto u = UserDto.builder().id(1L).name("User One").email("one@example.com").build();
        when(userService.getUsersByIds(List.of(1L))).thenReturn(List.of(u));

        List<CollaboratorUserDto> result = collaboratorService.getAllCollaboratorsBySourceId(1L, SourcingRequestType.REQUEST_FOR_QUOTE);

        assertEquals(1L, result.size());
        CollaboratorUserDto dto = result.getFirst();
        assertEquals(1L, dto.getUserId());
        assertEquals("User One", dto.getName());
        assertEquals("one@example.com", dto.getEmail());
    }

    @Test
    void getCollaboratorsBySourceId_returnsDtoList() {
        Collaborator c = Collaborator.builder().id(2L).sourceId(2L).sourceType(SourcingRequestType.REQUEST_FOR_PROPOSAL).userId(2L).role(CollaboratorRole.BD).active(true).build();
        when(collaboratorRepository.findAllBySourceIdAndSourceTypeAndActiveTrue(2L, SourcingRequestType.REQUEST_FOR_PROPOSAL))
                .thenReturn(List.of(c));

        List<CollaboratorDto> result = collaboratorService.getCollaboratorsBySourceId(2L, SourcingRequestType.REQUEST_FOR_PROPOSAL);
        assertEquals(1, result.size());
        assertEquals(2L, result.getFirst().getId());
        assertEquals(CollaboratorRole.BD, result.getFirst().getRole());
    }

    @Test
    void getCollaboratorBySourceIdAndUserId_found() {
        Collaborator c = Collaborator.builder().id(3L).sourceId(3L).sourceType(SourcingRequestType.REQUEST_FOR_QUOTE).userId(3L).role(CollaboratorRole.EDITOR).active(true).build();
        when(collaboratorRepository.findBySourceIdAndSourceTypeAndUserIdAndActiveTrue(3L, SourcingRequestType.REQUEST_FOR_QUOTE, 3L))
                .thenReturn(Optional.of(c));

        CollaboratorDto dto = collaboratorService.getCollaboratorBySourceIdAndUserId(3L, SourcingRequestType.REQUEST_FOR_QUOTE, 3L);
        assertEquals(3L, dto.getId());
        assertEquals(3L, dto.getUserId());
    }

    @Test
    void findCollaboratorBySourceIdAndUserId_notFound_throws() {
        when(collaboratorRepository.findBySourceIdAndSourceTypeAndUserIdAndActiveTrue(eq(1L), eq(SourcingRequestType.REQUEST_FOR_PROPOSAL), eq(5L)))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> collaboratorService.findCollaboratorBySourceIdAndUserId(1L, SourcingRequestType.REQUEST_FOR_PROPOSAL, 5L));
    }

    @Test
    void createOwner_duplicate_throws() {
        when(collaboratorRepository.existsBySourceIdAndSourceTypeAndUserIdAndActiveTrue(1L, SourcingRequestType.REQUEST_FOR_QUOTE, 1L))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> collaboratorService.createOwner(1L, SourcingRequestType.REQUEST_FOR_QUOTE, 1L));
    }

    @Test
    void createOwner_savesOwner() {
        when(collaboratorRepository.existsBySourceIdAndSourceTypeAndUserIdAndActiveTrue(1L, SourcingRequestType.REQUEST_FOR_QUOTE, 1L))
                .thenReturn(false);

        when(collaboratorRepository.save(any(Collaborator.class))).thenAnswer(i -> i.getArgument(0));

        collaboratorService.createOwner(1L, SourcingRequestType.REQUEST_FOR_QUOTE, 1L);

        verify(collaboratorRepository).save(argThat(c -> 1L == c.getUserId() && CollaboratorRole.OWNER.equals(c.getRole()) && c.isActive()));
    }

    @Test
    void addCollaborator_cannotAddOwnerRole() {
        assertThrows(BadRequestException.class, () -> collaboratorService.addCollaborator(1L, SourcingRequestType.REQUEST_FOR_PROPOSAL, "a@b.com", CollaboratorRole.OWNER, "ownerId"));
    }

    @Test
    void addCollaborator_duplicate_throws() {
        UserDto user = UserDto.builder().id(4L).email("dup@example.com").name("Dup").build();
        when(userService.getUserByEmail("dup@example.com")).thenReturn(user);
        when(collaboratorRepository.existsBySourceIdAndSourceTypeAndUserIdAndActiveTrue(1L, SourcingRequestType.REQUEST_FOR_QUOTE, 4L))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> collaboratorService.addCollaborator(1L, SourcingRequestType.REQUEST_FOR_QUOTE, "dup@example.com", CollaboratorRole.EDITOR, "ownerId"));
    }

    @Test
    void addCollaborator_success_sendsEmail_and_publishesEvent() {
        UserDto user = UserDto.builder().id(5L).email("u5@example.com").name("User5").build();
        when(userService.getUserByEmail("u5@example.com")).thenReturn(user);
        when(collaboratorRepository.existsBySourceIdAndSourceTypeAndUserIdAndActiveTrue(5L, SourcingRequestType.REQUEST_FOR_QUOTE, 5L))
                .thenReturn(false);

        Collaborator saved = Collaborator.builder().id(5L).sourceId(5L).sourceType(SourcingRequestType.REQUEST_FOR_QUOTE).userId(5L).role(CollaboratorRole.EDITOR).active(true).build();
        when(collaboratorRepository.save(any(Collaborator.class))).thenReturn(saved);

        UserDto owner = UserDto.builder().id(101L).name("Owner Name").company("OwnerCo").build();
        when(userService.getUserById(101L)).thenReturn(owner);

        when(collaboratorRepository.findAllBySourceIdAndSourceTypeAndActiveTrue(5L, SourcingRequestType.REQUEST_FOR_QUOTE))
                .thenReturn(List.of(saved));

        CollaboratorUserDto result = collaboratorService.addCollaborator(5L, SourcingRequestType.REQUEST_FOR_QUOTE, "u5@example.com", CollaboratorRole.EDITOR, "101");

        assertEquals(5L, result.getUserId());
        verify(emailService).sendCollaboratorInvite(eq("u5@example.com"), eq("User5"), eq("Owner Name"), eq("OwnerCo"), eq("EDITOR"), contains("/request-for-quote/quote-details"), eq(SourcingRequestType.REQUEST_FOR_QUOTE));
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void updateCollaboratorRole_cannotAssignOwner() {
        assertThrows(BadRequestException.class, () -> collaboratorService.updateCollaboratorRole(1L, SourcingRequestType.REQUEST_FOR_QUOTE, 5L, CollaboratorRole.OWNER, 1L));
    }

    @Test
    void updateCollaboratorRole_success_updatesAndSendsEmail() {
        Collaborator existing = Collaborator.builder()
                .id(6L)
                .sourceId(5L)
                .sourceType(SourcingRequestType.REQUEST_FOR_PROPOSAL)
                .userId(5L)
                .role(CollaboratorRole.EDITOR)
                .active(true).build();
        when(collaboratorRepository.findBySourceIdAndSourceTypeAndUserIdAndActiveTrue(eq(5L), eq(SourcingRequestType.REQUEST_FOR_PROPOSAL), eq(5L)))
                .thenReturn(Optional.of(existing));
        when(collaboratorRepository.save(any(Collaborator.class))).thenAnswer(i -> i.getArgument(0));

        UserDto collaboratorUser = UserDto.builder().id(5L).email("u6@example.com").name("U6").build();
        when(userService.getUserById(5L)).thenReturn(collaboratorUser);

        UserDto ownerUser = UserDto.builder().id(4L).name("Owner Name").company("OwnerCo").build();
        when(userService.getUserById(4L)).thenReturn(ownerUser);

        CollaboratorDto updated = collaboratorService.updateCollaboratorRole(5L, SourcingRequestType.REQUEST_FOR_PROPOSAL, 5L, CollaboratorRole.BD, 4L);

        assertEquals(CollaboratorRole.BD, updated.getRole());
        verify(emailService).sendCollaboratorAccessChange(eq("u6@example.com"), eq("U6"), eq("Owner Name"), eq("OwnerCo"), eq("BD"), contains("/request-for-proposal/proposal-details"), eq(SourcingRequestType.REQUEST_FOR_PROPOSAL));
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void deleteCollaborator_setsActiveFalse_andPublishesEvent() {
        Collaborator existing = Collaborator
                .builder()
                .id(1L)
                .sourceId(1L)
                .sourceType(SourcingRequestType.REQUEST_FOR_QUOTE)
                .userId(1L)
                .role(CollaboratorRole.EDITOR)
                .active(true).build();
        when(collaboratorRepository.findBySourceIdAndSourceTypeAndUserIdAndActiveTrue(eq(1L), eq(SourcingRequestType.REQUEST_FOR_QUOTE), eq(1L)))
                .thenReturn(Optional.of(existing));

        collaboratorService.deleteCollaborator(1L, SourcingRequestType.REQUEST_FOR_QUOTE, 1L);

        assertFalse(existing.isActive());
        verify(eventPublisher).publishEvent(any());
        verify(collaboratorRepository).save(existing);
    }
}
