package ai.leadplus.application.users;

import ai.leadplus.application.aws.EmailService;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.InvalidOldPasswordException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.tenants.TenantService;
import ai.leadplus.application.workspaces.WorkspaceService;
import ai.leadplus.domain.users.User;
import ai.leadplus.domain.users.UserRepository;
import ai.leadplus.domain.users.UserRole;
import ai.leadplus.domain.users.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private TenantService tenantService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserSearchService userSearchService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "clientUrl", "http://localhost:3000");
    }

    @Test
    void createUser_shouldCreateUserAndWorkspaceAndSetWorkspaceId_whenEmailNotExists() {
        UserDto input = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("password")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("encodedPass")
                .active(true)
                .draft(false)
                .tenantId(1L)
                .workspaceId(1L)
                .build();

        when(userRepository.findByEmailAndActiveTrue("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = userService.createUser(input);

        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());

        verify(userRepository, times(2)).save(any(User.class));
    }


    @Test
    void createUser_shouldThrowException_whenActiveNonDraftUserAlreadyExists() {
        UserDto input = UserDto.builder()
                .email("john@example.com")
                .name("John")
                .password("password")
                .build();

        User existingUser = User.builder()
                .email("john@example.com")
                .draft(false)
                .active(true)
                .build();

        when(userRepository.findByEmailAndActiveTrue("john@example.com")).thenReturn(Optional.of(existingUser));

        assertThrows(BadRequestException.class, () -> userService.createUser(input));
        verify(userRepository).findByEmailAndActiveTrue("john@example.com");
        verify(userRepository, never()).save(any());
        verifyNoInteractions(tenantService, workspaceService);
    }

    @Test
    void updateUser_shouldUpdateUser_whenUserExists() {
        User existing = User.builder()
                .id(1L)
                .name("Old Name")
                .phoneNumber("+1 555 000 0000")
                .company("Old Company")
                .email("john@example.com")
                .active(true)
                .build();

        UserDto updateDto = UserDto.builder()
                .name("New Name")
                .phoneNumber("+1 555 111 2222")
                .company("New Company")
                .build();

        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(1L, updateDto);

        assertEquals("New Name", result.getName());
        assertEquals("+1 555 111 2222", result.getPhoneNumber());
        assertEquals("New Company", result.getCompany());
        assertEquals("New Name", existing.getName());
        assertEquals("+1 555 111 2222", existing.getPhoneNumber());
        assertEquals("New Company", existing.getCompany());
        verify(userRepository).save(existing);
    }

    @Test
    void updateUser_shouldUpdateOnlyProvidedFields_whenPartialPayloadIsSent() {
        User existing = User.builder()
                .id(1L)
                .name("Old Name")
                .phoneNumber("+1 555 000 0000")
                .company("Old Company")
                .email("john@example.com")
                .active(true)
                .build();

        UserDto updateDto = UserDto.builder()
                .phoneNumber("+1 555 999 8888")
                .build();

        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(1L, updateDto);

        assertEquals("Old Name", result.getName());
        assertEquals("+1 555 999 8888", result.getPhoneNumber());
        assertEquals("Old Company", result.getCompany());
        assertEquals("Old Name", existing.getName());
        assertEquals("+1 555 999 8888", existing.getPhoneNumber());
        assertEquals("Old Company", existing.getCompany());
        verify(userRepository).save(existing);
    }

    @Test
    void updateUser_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(1L, UserDto.builder().name("New Name").build()));
        verify(userRepository).findByIdAndActiveTrue(1L);
    }

    @Test
    void updatePassword_shouldUpdatePassword_whenOldPasswordMatches() {
        User existing = User.builder()
                .id(1L)
                .password("encodedOld")
                .active(true)
                .build();

        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("oldPass", "encodedOld")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNew");

        userService.updatePassword(1L, "oldPass", "newPass");

        verify(passwordEncoder).encode("newPass");
        verify(userRepository).save(existing);
    }

    @Test
    void updatePassword_shouldThrowException_whenOldPasswordInvalid() {
        User existing = User.builder()
                .id(1L)
                .password("encodedOld")
                .active(true)
                .build();

        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrongOld", "encodedOld")).thenReturn(false);

        assertThrows(InvalidOldPasswordException.class,
                () -> userService.updatePassword(1L, "wrongOld", "newPass"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_shouldSetUserInactive_whenUserExists() {
        User existing = User.builder()
                .id(1L)
                .active(true)
                .build();

        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));

        userService.deleteUser(1L);

        assertFalse(existing.isActive());
        verify(userRepository).save(existing);
    }

    @Test
    void deleteUser_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser(1L));
    }

    @Test
    void getUserById_shouldReturnUserDto_whenUserExists() {
        User user = User.builder().id(1L).name("John").email("john@example.com").active(true).build();
        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertEquals(1L, result.getId());
        verify(userRepository).findByIdAndActiveTrue(1L);
    }

    @Test
    void getUserById_shouldThrowException_whenNotFound() {
        when(userRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(1L));
    }

    @Test
    void getUserByEmail_shouldReturnUserDto_whenUserExists() {
        User user = User.builder().id(1L).name("John").email("john@example.com").active(true).build();
        when(userRepository.findByEmailAndActiveTrue("john@example.com")).thenReturn(Optional.of(user));

        UserDto result = userService.getUserByEmail("john@example.com");

        assertEquals(1L, result.getId());
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).findByEmailAndActiveTrue("john@example.com");
    }

    @Test
    void getUserByEmail_shouldThrowException_whenNotFound() {
        when(userRepository.findByEmailAndActiveTrue("john@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserByEmail("john@example.com"));
    }


    @Test
    void searchUsers_shouldReturnPagedUserDtoPage() {
        User user = User.builder().id(1L).tenantId(1L).name("John").email("john@example.com").active(true).build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page =
                new PageImpl<>(java.util.List.of(user), pageable, 1);

        when(userSearchService.searchUsers(
                anyString(),
                anyString(),
                anyList(),
                anyList(),
                any(Pageable.class)))
                .thenReturn(page);

        Page<UserDto> result =
                userService.searchUsers("1L", "john", java.util.List.of(UserRole.USER),
                        List.of(UserStatus.APPROVED), pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().getFirst().getId());
        verify(userSearchService).searchUsers(
                anyString(),
                eq("john"),
                anyList(),
                anyList(),
                eq(pageable));
    }

    @Test
    void approveUser_shouldUpdateStatusAndReturnDto() {
        User existing = User.builder()
                .id(1L)
                .email("john@example.com")
                .name("John")
                .status(UserStatus.PENDING)
                .roles(List.of(UserRole.USER))
                .active(true)
                .build();

        when(userRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.approveUser(1L);

        assertEquals(1L, result.getId());
        assertEquals(UserStatus.APPROVED, existing.getStatus());
        assertEquals(List.of(UserRole.USER), existing.getRoles());

        verify(userRepository).save(existing);
        verify(emailService).sendUserApprovedEmail(
                eq(existing.getEmail()),
                eq(existing.getName()),
                eq("http://localhost:3000")
        );
    }


    @Test
    void rejectUser_shouldUpdateStatusAndReturnDto() {
        User existing = User.builder()
                .id(1L)
                .email("john@example.com")
                .name("John")
                .status(UserStatus.PENDING)
                .roles(List.of(UserRole.USER))
                .active(true)
                .build();

        when(userRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.rejectUser(1L);

        assertEquals(1L, result.getId());
        assertEquals(UserStatus.REJECTED, existing.getStatus());

        verify(userRepository).save(existing);
        verify(emailService).sendUserRejectedEmail(
                eq("john@example.com"),
                eq("John"),
                eq(List.of())
        );
    }


    @Test
    void approveUser_shouldThrow_whenUserNotFound() {
        when(userRepository.findByIdAndActiveTrue(0L)).thenReturn(java.util.Optional.empty());

        Executable call = () -> userService.approveUser(0L);
        assertThrows(ResourceNotFoundException.class, call);

        verify(userRepository).findByIdAndActiveTrue(0L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectUser_shouldThrow_whenUserNotFound() {
        when(userRepository.findByIdAndActiveTrue(0L)).thenReturn(java.util.Optional.empty());

        Executable call = () -> userService.rejectUser(0L);
        assertThrows(ResourceNotFoundException.class, call);

        verify(userRepository).findByIdAndActiveTrue(0L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void approveUser_shouldPreserveAdminRole() {
        User adminUser = User.builder()
                .id(101L)
                .email("admin@example.com")
                .name("Admin User")
                .status(UserStatus.PENDING)
                .roles(List.of(UserRole.ADMIN))
                .active(true)
                .build();

        when(userRepository.findByIdAndActiveTrue(101L)).thenReturn(Optional.of(adminUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.approveUser(101L);

        assertEquals(UserStatus.APPROVED, adminUser.getStatus(), "Status should be APPROVED");
        assertTrue(adminUser.getRoles().contains(UserRole.ADMIN), "Admin role should be preserved");
        verify(userRepository).save(adminUser);
        verify(emailService).sendUserApprovedEmail(
                eq(adminUser.getEmail()),
                eq(adminUser.getName()),
                eq("http://localhost:3000")
        );
    }

    @Test
    void approveUser_shouldUpgradeGuestToUserRole() {
        User guestUser = User.builder()
                .id(102L)
                .email("guest@example.com")
                .name("Guest User")
                .status(UserStatus.PENDING)
                .roles(List.of(UserRole.GUEST))
                .active(true)
                .build();

        when(userRepository.findByIdAndActiveTrue(102L)).thenReturn(Optional.of(guestUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.approveUser(102L);

        assertEquals(UserStatus.APPROVED, guestUser.getStatus(), "Status should be APPROVED");
        assertTrue(guestUser.getRoles().contains(UserRole.USER), "Guest should be upgraded to USER role");
        verify(userRepository).save(guestUser);
        verify(emailService).sendUserApprovedEmail(
                eq(guestUser.getEmail()),
                eq(guestUser.getName()),
                eq("http://localhost:3000")
        );
    }
}
