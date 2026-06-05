package ai.leadplus.application.users;

import ai.leadplus.application.auth.ForgotPasswordDto;
import ai.leadplus.application.aws.EmailService;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.InvalidOldPasswordException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.users.User;
import ai.leadplus.domain.users.UserRepository;
import ai.leadplus.domain.users.UserRole;
import ai.leadplus.domain.users.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSearchService userSearchService;
    private final EmailService emailService;

    @Value("${client.url}")
    private String clientUrl;

    public UserDto createUser(UserDto userDto) {
        Optional<User> optionalUser = userRepository.findByEmailAndActiveTrue(userDto.getEmail());

        User user;
        boolean isNewUser = false;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            user.setRoles(new ArrayList<>(List.of(UserRole.CUSTOMER)));
            if (!user.isDraft()) {
                throw new BadRequestException("Unable to create account. Please verify the details and try again.");
            }
        } else {
            user = userDto.toEntity();
            user.setStatus(UserStatus.APPROVED);
            user.setRoles(new ArrayList<>(List.of(UserRole.CUSTOMER)));
            isNewUser = true;
        }

        user.setName(userDto.getName());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setCompany(userDto.getCompany());
        user.setPassword(StringUtils.hasText(userDto.getPassword()) ? passwordEncoder.encode(userDto.getPassword()) : null);
        user.setDraft(false);
        user.setActive(true);
        if (isNewUser && CollectionUtils.isEmpty(user.getIdentityProviders())) {
            user.setEmailVerified(false);
            sendVerificationEmail(user);
        }
        user = userRepository.save(user);
        return UserDto.fromEntity(user);
    }

    public UserDto createVendorUser(UserDto userDto) {

        Optional<User> optionalUser = userRepository.findByEmailAndActiveTrue(userDto.getEmail());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getRoles().contains(UserRole.VENDOR)) {
                throw new BadRequestException("Unable to create account. Please verify the details and try again.");
            }
            user.getRoles().add(UserRole.VENDOR);
            userRepository.save(user);
            return UserDto.fromEntity(user);
        }
        User user = userDto.toEntity();
        user.setPassword(StringUtils.hasText(userDto.getPassword()) ? passwordEncoder.encode(userDto.getPassword()) : null);
        user.setStatus(UserStatus.APPROVED);
        user.setRoles(new ArrayList<>(List.of(UserRole.VENDOR, UserRole.CUSTOMER)));
        user.setDraft(false);
        user.setActive(true);
        user.setEmailVerified(false);
        sendVerificationEmail(user);
        user = userRepository.save(user);

        return UserDto.fromEntity(user);
    }

    public UserDto updateUser(Long userId, UserDto updateDto) {
        User existing = findUserById(userId);
        if (StringUtils.hasText(updateDto.getName())) {
            existing.setName(updateDto.getName());
        }
        if (updateDto.getPhoneNumber() != null) {
            existing.setPhoneNumber(updateDto.getPhoneNumber());
        }
        if (updateDto.getCompany() != null) {
            existing.setCompany(updateDto.getCompany());
        }
        userRepository.save(existing);
        return UserDto.fromEntity(existing);
    }

    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User existing = findUserById(userId);

        if (!passwordEncoder.matches(oldPassword, existing.getPassword())) {
            throw new InvalidOldPasswordException("Invalid current password.");
        }
        existing.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(existing);
    }

    public void deleteUser(Long userId) {
        User existing = findUserById(userId);

        existing.setActive(false);
        userRepository.save(existing);
    }

    public Page<UserDto> searchUsers(String tenantId, String query, List<UserRole> userRoles, List<UserStatus> userStatuses, Pageable pageable) {
        return userSearchService.searchUsers(tenantId, query, userRoles, userStatuses, pageable)
                .map(UserDto::fromEntity);
    }

    public UserDto approveUser(Long userId) {
        UserDto userDto = updateUserStatus(userId, UserStatus.APPROVED);
        emailService.sendUserApprovedEmail(userDto.getEmail(), userDto.getName(), clientUrl);
        return userDto;
    }

    public UserDto rejectUser(Long userId) {
        UserDto userDto = updateUserStatus(userId, UserStatus.REJECTED);
        emailService.sendUserRejectedEmail(userDto.getEmail(), userDto.getName(), List.of());
        return userDto;
    }

    private UserDto updateUserStatus(Long userId, UserStatus status) {
        User existing = findUserById(userId);
        existing.setStatus(status);

        if (status == UserStatus.APPROVED) {
            List<UserRole> roles = existing.getRoles();

            if (roles == null || roles.isEmpty()) {
                roles = new ArrayList<>();
            } else {
                roles = new ArrayList<>(roles);
            }

            if (!roles.contains(UserRole.ADMIN) && !roles.contains(UserRole.USER)) {
                roles.add(UserRole.USER);
            }

            existing.setRoles(roles);
        }
        userRepository.save(existing);
        return UserDto.fromEntity(existing);
    }

    public UserDto getUserById(Long userId) {
        return UserDto.fromEntity(findUserById(userId));
    }

    public UserDto getUserByEmail(String email) {
        return UserDto.fromEntity(findUserByEmail(email));
    }

    public UserDto saveUser(UserDto userDto) {
        User user = userRepository.save(userDto.toEntity());
        return UserDto.fromEntity(user);
    }


    private User findUserById(Long userId) {
        return userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId + "."));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email + "."));
    }

    public UserDto verifyForgotPassword(ForgotPasswordDto forgotPasswordDto) {
        User user = userRepository.findByVerificationTokenAndActiveTrue(forgotPasswordDto.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired verification token"));
        user.setPassword(passwordEncoder.encode(forgotPasswordDto.getPassword()));
        user.setVerificationToken(null);
        return UserDto.fromEntity(userRepository.save(user));
    }

    public Optional<UserDto> getOptionalUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmailAndActiveTrue(email);
        return user.map(UserDto::fromEntity);
    }

    public UserDto createInviteUser(UserDto userDto) {
        User user = userRepository.save(userDto.toEntity());
        return UserDto.fromEntity(user);
    }

    public List<UserDto> getUsersByIds(Set<Long> userIds) {
        return userRepository.findAllByIdInAndActiveTrue(userIds)
                .stream()
                .map(UserDto::fromEntity)
                .toList();
    }

    public List<UserDto> getUsersByIds(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) return List.of();

        userIds = userIds.stream().distinct().toList();
        return userRepository.findAllByIdInAndActiveTrue(userIds).stream()
                .map(UserDto::fromEntity)
                .toList();
    }

    public UserDto getByEmailVerificationToken(String token) {
        User user = userRepository.findByEmailVerificationTokenAndActiveTrue(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email verification token"));
        return UserDto.fromEntity(user);
    }

    private void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        user.setEmailVerificationToken(token);
        user = userRepository.save(user);
        String verificationLink = String.format(
                "%s/verify-email?token=%s",
                clientUrl, user.getEmailVerificationToken()
        );
        emailService.sendEmailVerificationEmail(user.getEmail(), user.getName(), verificationLink);
    }

    public void resendEmailVerification(Long userId) {
        User user = findUserById(userId);
        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified.");
        }
        sendVerificationEmail(user);
    }

    public List<UserDto> getUsersByUserIds(List<Long> userIds) {
        return userRepository.findAllByIdInAndActiveAndDraftFalse(userIds, true).stream()
                .map(UserDto::fromEntity)
                .toList();
    }

    public long countActiveByTenantId(Long tenantId) {
        return userRepository.countByTenantIdAndActiveTrue(tenantId);
    }
}
