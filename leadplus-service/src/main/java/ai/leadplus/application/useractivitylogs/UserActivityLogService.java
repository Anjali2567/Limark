package ai.leadplus.application.useractivitylogs;

import ai.leadplus.domain.useractivitylogs.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserActivityLogService {

    private final UserActivityLogRepository repository;

    @Async
    public void log(Long tenantId,
                    Long workspaceId,
                    Long userId,
                    String username,
                    String message,
                    LogLevel logLevel,
                    UserActivityLogType type) {

        UserActivityLog log = UserActivityLog.builder()
                .tenantId(tenantId)
                .workspaceId(workspaceId)
                .userId(userId)
                .username(username)
                .message(message)
                .logLevel(logLevel)
                .type(type)
                .build();

        repository.save(log);
    }
}