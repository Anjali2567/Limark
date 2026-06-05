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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollaboratorService {

    private final CollaboratorRepository collaboratorRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;

    @Value("${client.url}")
    private String clientUrl;

    public List<CollaboratorUserDto> getAllCollaboratorsBySourceId(Long sourceId, SourcingRequestType sourceType) {
        List<Collaborator> collaborators =
                collaboratorRepository.findAllBySourceIdAndSourceTypeAndActiveTrue(sourceId, sourceType);
        List<Long> userIds = collaborators.stream()
                .map(Collaborator::getUserId)
                .toList();
        List<UserDto> userDtoList = userService.getUsersByIds(userIds);
        Map<Long, UserDto> userDtoMap = userDtoList.stream()
                .collect(Collectors.toMap(UserDto::getId, u -> u));
        return collaborators.stream()
                .map(c -> {
                    UserDto userDto = userDtoMap.getOrDefault(c.getUserId(), new UserDto());
                    return CollaboratorUserDto.toDto(c, userDto);
                })
                .toList();
    }

    public List<CollaboratorDto> getCollaboratorsBySourceId(Long sourceId, SourcingRequestType sourceType) {
        return collaboratorRepository.findAllBySourceIdAndSourceTypeAndActiveTrue(sourceId, sourceType)
                .stream()
                .map(CollaboratorDto::toDto)
                .toList();
    }

    public CollaboratorDto getCollaboratorBySourceIdAndUserId(Long sourceId, SourcingRequestType sourceType, Long userId) {
        return CollaboratorDto.toDto(findCollaboratorBySourceIdAndUserId(sourceId, sourceType, userId));
    }

    public Collaborator findCollaboratorBySourceIdAndUserId(Long sourceId, SourcingRequestType sourceType, Long userId) {
        return collaboratorRepository.findBySourceIdAndSourceTypeAndUserIdAndActiveTrue(sourceId, sourceType, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Collaborator not found for sourceId: " + sourceId + " and userId: " + userId));
    }

    public void createOwner(Long sourceId, SourcingRequestType sourceType, Long userId) {
        if (collaboratorRepository.existsBySourceIdAndSourceTypeAndUserIdAndActiveTrue(sourceId, sourceType, userId)) {
            throw new DuplicateResourceException("User is already a collaborator");
        }
        Collaborator collaborator = Collaborator.builder()
                .sourceId(sourceId)
                .sourceType(sourceType)
                .userId(userId)
                .role(CollaboratorRole.OWNER)
                .active(true)
                .build();
        collaboratorRepository.save(collaborator);
    }

    @Transactional
    public CollaboratorUserDto addCollaborator(Long sourceId,
                                               SourcingRequestType sourceType,
                                               String email,
                                               CollaboratorRole role,
                                               String ownerUserId) {
        if (CollaboratorRole.OWNER.equals(role)) {
            throw new BadRequestException("Cannot add collaborator with OWNER role");
        }
        UserDto userDto = userService.getUserByEmail(email);

        if (collaboratorRepository.existsBySourceIdAndSourceTypeAndUserIdAndActiveTrue(sourceId, sourceType, userDto.getId())) {
            throw new DuplicateResourceException("User is already a collaborator");
        }

        Collaborator collaborator = Collaborator.builder()
                .sourceId(sourceId)
                .sourceType(sourceType)
                .userId(userDto.getId())
                .role(role)
                .active(true)
                .build();
        Collaborator saved = collaboratorRepository.save(collaborator);

        UserDto ownerUser = userService.getUserById(Long.parseLong(ownerUserId));
        List<String> collaboratorUserIds = getCollaboratorsBySourceId(sourceId, sourceType)
                .stream()
                .map(c -> String.valueOf(c.getUserId()))
                .toList();

        String inviteLink = generateInviteLink(sourceId, sourceType);

        String userName = userDto.getName() != null ? userDto.getName() : email;
        emailService.sendCollaboratorInvite(
                email,
                userName,
                ownerUser.getName(),
                ownerUser.getCompany(),
                role.name(),
                inviteLink,
                sourceType
        );

        CollaboratorCreatedEvent event = new CollaboratorCreatedEvent(
                this,
                CollaboratorDto.toDto(saved),
                ownerUser,
                userDto,
                collaboratorUserIds
        );
        eventPublisher.publishEvent(event);

        return CollaboratorUserDto.toDto(saved, userDto);
    }

    @Transactional
    public CollaboratorDto updateCollaboratorRole(Long sourceId,
                                                  SourcingRequestType sourceType,
                                                  Long userId,
                                                  CollaboratorRole role,
                                                  Long ownerUserId) {
        if (CollaboratorRole.OWNER.equals(role)) {
            throw new BadRequestException("Cannot assign OWNER role");
        }
        Collaborator collaborator = findCollaboratorBySourceIdAndUserId(sourceId, sourceType, userId);
        collaborator.setRole(role);
        Collaborator updated = collaboratorRepository.save(collaborator);
        CollaboratorDto updatedDto = CollaboratorDto.toDto(updated);

        UserDto userDto = userService.getUserById(userId);
        UserDto ownerUser = userService.getUserById(ownerUserId);
        String inviteLink = generateInviteLink(sourceId, sourceType);
        emailService.sendCollaboratorAccessChange(
                userDto.getEmail(),
                userDto.getName(),
                ownerUser.getName(),
                ownerUser.getCompany(),
                role.name(),
                inviteLink,
                sourceType
        );

        eventPublisher.publishEvent(new CollaboratorUpdateEvent(this, updatedDto));
        return updatedDto;
    }

    @Transactional
    public void deleteCollaborator(Long sourceId, SourcingRequestType sourceType, Long userId) {
        Collaborator collaborator = findCollaboratorBySourceIdAndUserId(sourceId, sourceType, userId);
        collaborator.setActive(false);
        CollaboratorDeleteEvent event = new CollaboratorDeleteEvent(this, CollaboratorDto.toDto(collaborator));
        eventPublisher.publishEvent(event);
        collaboratorRepository.save(collaborator);
    }

    private String generateInviteLink(Long sourceId, SourcingRequestType sourceType) {
        String path = switch (sourceType) {
            case REQUEST_FOR_QUOTE -> "/request-for-quote/quote-details";
            case REQUEST_FOR_PROPOSAL -> "/request-for-proposal/proposal-details";
        };
        return String.format("%s/customer%s?id=%s", clientUrl, path, sourceId);
    }
}
