package ai.leadplus.application.users;

import ai.leadplus.domain.users.User;
import ai.leadplus.domain.users.UserRepository;
import ai.leadplus.domain.users.UserRole;
import ai.leadplus.domain.users.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserSearchServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSearchService userSearchService;

    private User user;
    private User activeUser;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .roles(List.of(UserRole.USER))
                .status(UserStatus.PENDING)
                .active(true)
                .build();

        activeUser = User.builder()
                .id(2L)
                .name("Active User")
                .email("active@example.com")
                .active(true)
                .build();
    }

    @Test
    void searchUsers_shouldReturnPagedUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        Page<User> result = userSearchService.searchUsers(null, "john", List.of(UserRole.USER), List.of(UserStatus.PENDING), pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().getFirst().getName());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchUsers_shouldApplyActiveOnlyWhenNoFilters() {
        Pageable pageable = PageRequest.of(0, 5);
        when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(activeUser), pageable, 1));

        Page<User> result = userSearchService.searchUsers(null, null, List.of(), List.of(), pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Active User", result.getContent().getFirst().getName());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }
}
