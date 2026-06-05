package ai.leadplus.domain.useractivitylogs;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActivityLogRepository
        extends JpaRepository<UserActivityLog, Long> {
}
