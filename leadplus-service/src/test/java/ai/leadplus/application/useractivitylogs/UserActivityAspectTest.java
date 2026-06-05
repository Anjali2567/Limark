package ai.leadplus.application.useractivitylogs;

import ai.leadplus.api.common.UserValidator;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.domain.useractivitylogs.LogLevel;
import ai.leadplus.domain.useractivitylogs.UserActivityLogType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.lang.reflect.Method;

import static org.mockito.Mockito.*;

class UserActivityAspectTest {

    @Mock
    private UserActivityLogService userActivityLogService;

    @Mock
    private UserValidator userValidator;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @InjectMocks
    private UserActivityAspect aspect;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    static class TestService {

        @TrackUserActivity(
                message = "Created campaign",
                level = LogLevel.INFO,
                type = UserActivityLogType.CAMPAIGN
        )
        public String createCampaign() {
            return "SUCCESS";
        }
    }

    @Test
    void shouldLogUserActivity() throws Exception {

        TestService service = new TestService();
        Method method = TestService.class.getMethod("createCampaign");

        TrackUserActivity annotation =
                method.getAnnotation(TrackUserActivity.class);

        UserDto user = UserDto.builder()
                .id(1L)
                .workspaceId(1L)
                .tenantId(1L)
                .name("John")
                .build();

        when(userValidator.getAuthenticatedUser()).thenReturn(user);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.getTarget()).thenReturn(service);

        aspect.logActivity(joinPoint, annotation, "SUCCESS");

        verify(userActivityLogService).log(
                1L,
                1L,
                1L,
                "John",
                "Created campaign",
                LogLevel.INFO,
                UserActivityLogType.CAMPAIGN
        );
    }

    @Test
    void shouldNotThrowExceptionIfLoggingFails() throws Exception {

        TestService service = new TestService();
        Method method = TestService.class.getMethod("createCampaign");

        TrackUserActivity annotation =
                method.getAnnotation(TrackUserActivity.class);

        when(userValidator.getAuthenticatedUser())
                .thenThrow(new RuntimeException("Auth error"));

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.getTarget()).thenReturn(service);

        aspect.logActivity(joinPoint, annotation, "SUCCESS");

        verify(userActivityLogService, never()).log(
                any(), any(), any(), any(),
                any(), any(), any()
        );
    }
}
