package ai.leadplus.domain.mailboxes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MailboxRepository extends JpaRepository<Mailbox, Long> {

    Optional<Mailbox> findByUserIdAndWorkspaceIdAndTypeAndActiveTrue(Long userId, Long workspaceId, MailBoxType mailBoxType);

    Optional<Mailbox> findByIdAndActiveTrue(Long id);

    List<Mailbox> findAllByUserIdAndWorkspaceIdAndActiveTrue(Long userId, Long workspaceId);

    List<Mailbox> findAllByWorkspaceIdAndActiveTrue(Long workspaceId);

    @Modifying
    @Query("UPDATE Mailbox m SET m.emailsSentToday = 0 WHERE m.active = true AND m.emailsSentToday != :emailsSentToday")
    long resetEmailsSentTodayForActiveMailboxes(int emailsSentToday);
}
