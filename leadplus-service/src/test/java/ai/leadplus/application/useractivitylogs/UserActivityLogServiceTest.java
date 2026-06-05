package ai.leadplus.application.useractivitylogs;

import ai.leadplus.domain.useractivitylogs.LogLevel;
import ai.leadplus.domain.useractivitylogs.UserActivityLog;
import ai.leadplus.domain.useractivitylogs.UserActivityLogRepository;
import ai.leadplus.domain.useractivitylogs.UserActivityLogType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.mockito.Mockito.*;

class UserActivityLogServiceTest {

    @Mock
    private UserActivityLogRepository repository;

    @InjectMocks
    private UserActivityLogService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldSaveUserActivityLogSuccessfully() {

        service.log(
                1L,
                1L,
                1L,
                "John",
                "Created note",
                LogLevel.INFO,
                UserActivityLogType.NOTE
        );

        ArgumentCaptor<UserActivityLog> captor =
                ArgumentCaptor.forClass(UserActivityLog.class);

        verify(repository).save(captor.capture());

        UserActivityLog savedLog = captor.getValue();

        assert savedLog.getTenantId().equals(1L);
        assert savedLog.getWorkspaceId().equals(1L);
        assert savedLog.getUserId().equals(1L);
        assert savedLog.getUsername().equals("John");
        assert savedLog.getMessage().equals("Created note");
        assert savedLog.getLogLevel() == LogLevel.INFO;
        assert savedLog.getType() == UserActivityLogType.NOTE;
    }
}