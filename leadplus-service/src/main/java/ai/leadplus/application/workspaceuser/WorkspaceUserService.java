package ai.leadplus.application.workspaceuser;

import ai.leadplus.application.aws.EmailService;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.UnauthorizedException;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.application.workspaces.WorkspaceDto;
import ai.leadplus.application.workspaces.WorkspaceService;
import ai.leadplus.domain.users.UserRole;
import ai.leadplus.domain.users.UserStatus;
import ai.leadplus.domain.workspaceusers.WorkspaceUser;
import ai.leadplus.domain.workspaceusers.WorkspaceUserRepository;
import ai.leadplus.domain.workspaceusers.WorkspaceUserRole;
import ai.leadplus.domain.workspaceusers.WorkspaceUserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceUserService {

    @Value("${client.url}")
    private String clientUrl;

    private final UserService userService;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final EmailService emailService;
    private final WorkspaceService workspaceService;

    public WorkspaceUserDto findByWorkspaceIdAndUserId(Long workspaceId, Long userId) {
        return workspaceUserRepository
                .findByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, userId)
                .map(WorkspaceUserDto::fromEntity)
                .orElseThrow(() -> new UnauthorizedException("Invalid Workspace Id"));
    }

    @Transactional
    public WorkspaceUserDto inviteMember(Long tenantId, Long workspaceId, WorkspaceUserInviteDto workspaceUserInviteDto) {
        UserDto user;
        WorkspaceUserDto workspaceUser;
        Optional<UserDto> optionalUser = userService.getOptionalUserByEmail(workspaceUserInviteDto.getEmail());
        WorkspaceDto workspaceDto = workspaceService.getWorkspaceById(workspaceId);
        boolean userExists = optionalUser.isPresent();
        if (userExists) {
            user = optionalUser.get();
            if (!user.getTenantId().equals(tenantId)) {
                throw new BadRequestException("Unable to add this user to the workspace.");
            }
            user.setTenantId(tenantId);
            user.setWorkspaceId(workspaceId);
            validateUserNotInWorkspace(workspaceId, user.getId());
        } else {
            user = UserDto.builder()
                    .tenantId(tenantId)
                    .workspaceId(workspaceId)
                    .email(workspaceUserInviteDto.getEmail())
                    .name(workspaceUserInviteDto.getName())
                    .roles(List.of(UserRole.USER))
                    .draft(true)
                    .active(true)
                    .status(UserStatus.APPROVED)
                    .build();
            user = userService.createInviteUser(user);
        }
        String invitationToken = UUID.randomUUID().toString();
        workspaceUser = createInvitedWorkspaceMember(user, workspaceId, invitationToken);

        String invitationLink = String.format(
                "%s/workspace-invite?token=%s&userExists=%s",
                clientUrl,
                invitationToken,
                userExists);

        emailService.sendWorkspaceInvitationEmail(user.getEmail(), user.getName(), workspaceDto.getName(), invitationLink);
        return workspaceUser;
    }

    public WorkspaceUserDto createInvitedWorkspaceMember(UserDto userDto, Long workspaceId, String invitationToken) {
        WorkspaceUser workspaceUser = WorkspaceUser.builder()
                .tenantId(userDto.getTenantId())
                .workspaceId(workspaceId)
                .userId(userDto.getId())
                .workspaceUserRole(WorkspaceUserRole.MEMBER)
                .workspaceUserStatus(WorkspaceUserStatus.INVITED)
                .invitationToken(invitationToken)
                .active(true)
                .build();
        workspaceUser = workspaceUserRepository.save(workspaceUser);
        return WorkspaceUserDto.fromEntity(workspaceUser);
    }

    public void createWorkspaceOwner(UserDto userDto, Long workspaceId) {
        WorkspaceUser workspaceUser = WorkspaceUser.builder()
                .tenantId(userDto.getTenantId())
                .workspaceId(workspaceId)
                .userId(userDto.getId())
                .workspaceUserRole(WorkspaceUserRole.OWNER)
                .workspaceUserStatus(WorkspaceUserStatus.ACCEPTED)
                .active(true)
                .build();
        workspaceUserRepository.save(workspaceUser);
    }

    public Page<WorkspaceUserListDto> searchWorkspaceUsers(Long workspaceId, Pageable pageable) {
        Page<WorkspaceUser> workspaceUsers = workspaceUserRepository.findByWorkspaceIdAndActiveTrue(workspaceId, pageable);

        Set<Long> uniqueUserIds = workspaceUsers
                .map(WorkspaceUser::getUserId)
                .toSet();

        Map<Long, UserDto> userMap = userService.getUsersByIds(uniqueUserIds)
                .stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        return workspaceUsers.map(workspaceUser -> {
            UserDto userDto = userMap.get(workspaceUser.getUserId());
            return WorkspaceUserListDto.from(WorkspaceUserDto.fromEntity(workspaceUser), userDto);
        });
    }


    private void validateUserNotInWorkspace(Long workspaceId, Long userId) {
        if (workspaceUserRepository.existsByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, userId)) {
            throw new BadRequestException("This user is already a member of the workspace.");
        }
    }

    public List<WorkspaceUserDetailsDto> getAllUserWorkspaces(Long userId) {
        List<WorkspaceUser> workspaceUserDtoList = workspaceUserRepository.findActiveWorkspaceUsersByUserId(userId);

        Set<Long> workspaceIds = workspaceUserDtoList.stream()
                .map(WorkspaceUser::getWorkspaceId)
                .collect(Collectors.toSet());

        Map<Long, WorkspaceDto> workspaceMap = workspaceService.getAllWorkspacesByIds(workspaceIds);

        return workspaceUserDtoList.stream().map(dto ->
                WorkspaceUserDetailsDto.fromDto(dto, workspaceMap.get(dto.getWorkspaceId()).getName())
        ).toList();
    }

    public void acceptInvitation(String token) {
        WorkspaceUser workspaceUser = workspaceUserRepository
                .findByInvitationToken(token)
                .orElseThrow(() -> new BadRequestException("Invitation not found"));

        LocalDateTime invitedAt = workspaceUser.getUpdatedAt();
        LocalDateTime now = LocalDateTime.now();

        if (invitedAt == null) {
            throw new BadRequestException("Invalid invitation timestamp");
        }

        if (invitedAt.plusHours(24).isBefore(now)) {
            throw new BadRequestException("Invitation token has expired");
        }

        if (!workspaceUser.isActive() || !workspaceUser.getWorkspaceUserStatus().equals(WorkspaceUserStatus.INVITED)) {
            throw new BadRequestException("Your invitation has been removed");
        }

        workspaceUser.setInvitationToken(null);
        workspaceUser.setWorkspaceUserStatus(WorkspaceUserStatus.ACCEPTED);
        workspaceUserRepository.save(workspaceUser);

        UserDto user = userService.getUserById(workspaceUser.getUserId());

        user.setEmailVerified(true);
        userService.saveUser(user);
    }

    public void revokeAccess(Long workspaceId, Long userId) {
        WorkspaceUserDto workspaceUser = findByWorkspaceIdAndUserId(workspaceId, userId);

        if (workspaceUser.getRole().equals(WorkspaceUserRole.OWNER)) {
            throw new BadRequestException("Workspace owners cannot have their access revoked.");
        }

        workspaceUser.setActive(false);
        workspaceUser.setStatus(WorkspaceUserStatus.REVOKED);
        workspaceUserRepository.save(workspaceUser.toEntity());
    }

    @Transactional
    public WorkspaceUserDto updateWorkspaceUserRole(Long workspaceId, Long userId, WorkspaceUserRole role) {
        if (role != WorkspaceUserRole.MEMBER && role != WorkspaceUserRole.WORKSPACE_ADMIN) {
            throw new BadRequestException("Only MEMBER and WORKSPACE_ADMIN roles can be assigned.");
        }

        WorkspaceUserDto workspaceUser = findByWorkspaceIdAndUserId(workspaceId, userId);

        if (workspaceUser.getRole() == WorkspaceUserRole.OWNER) {
            throw new BadRequestException("Workspace owners cannot be reassigned.");
        }

        if (workspaceUser.getStatus() != WorkspaceUserStatus.ACCEPTED) {
            throw new BadRequestException("Only active workspace members can be reassigned.");
        }

        workspaceUser.setRole(role);
        workspaceUserRepository.save(workspaceUser.toEntity());
        return workspaceUser;
    }

    @Transactional
    public WorkspaceUserDto transferOwnership(Long workspaceId, Long newOwnerUserId, UserDto currentUser) {

        if (Objects.equals(newOwnerUserId, currentUser.getId())) {
            throw new BadRequestException("You cannot transfer workspace ownership to yourself.");
        }

        WorkspaceDto workspace = workspaceService.getWorkspaceById(workspaceId);
        WorkspaceUserDto currentOwner = findByWorkspaceIdAndUserId(workspaceId, currentUser.getId());
        WorkspaceUserDto newOwner = findByWorkspaceIdAndUserId(workspaceId, newOwnerUserId);

        if (newOwner.getStatus() != WorkspaceUserStatus.ACCEPTED) {
            throw new BadRequestException("Ownership can only be transferred to an active workspace member.");
        }

        currentOwner.setRole(WorkspaceUserRole.MEMBER);
        workspaceUserRepository.save(currentOwner.toEntity());

        newOwner.setRole(WorkspaceUserRole.OWNER);
        workspaceUserRepository.save(newOwner.toEntity());

        workspace.setOwnerId(newOwner.getUserId());
        workspaceService.saveWorkspace(workspace);

        return newOwner;
    }

    public List<WorkspaceUserDto> getAllWorkspaceUsersByUserIds(Set<Long> userIds) {
        List<WorkspaceUser> workspaceUsers = workspaceUserRepository.findAllByUserIdInAndActiveTrue(userIds);
        return workspaceUsers.stream().map(WorkspaceUserDto::fromEntity).collect(Collectors.toList());
    }

    public List<WorkspaceUserDto> getAllWorkspaceUsersByWorkspaceIds(List<Long> workspaceIds) {
        List<WorkspaceUser> workspaceUsers = workspaceUserRepository.findAllByWorkspaceIdInAndActiveTrue(workspaceIds);
        return workspaceUsers.stream().map(WorkspaceUserDto::fromEntity).collect(Collectors.toList());
    }

    public long countActiveByTenantId(Long tenantId) {
        return workspaceUserRepository.countByTenantIdAndActiveTrue(tenantId);
    }
}
