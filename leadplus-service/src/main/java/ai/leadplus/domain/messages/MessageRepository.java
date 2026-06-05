package ai.leadplus.domain.messages;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long>, JpaSpecificationExecutor<Message> {

    List<Message> findTop5ByConversationIdOrderByCreatedAtDesc(String conversationId);

    List<Message> findAllByConversationIdOrderByCreatedAtAsc(String conversationId);
    List<Message> findAllByConversationIdAndTypeOrderByCreatedAtAsc(String conversationId, MessageType type);

    @Query("SELECT m FROM Message m " +
           "WHERE m.id IN (" +
           "  SELECT MIN(m2.id) FROM Message m2 " +
           "  WHERE m2.tenantId = :tenantId " +
           "  AND m2.workspaceId = :workspaceId " +
           "  AND m2.userId = :userId " +
           "  AND m2.type = :type " +
           "  GROUP BY m2.conversationId" +
           ")")
    Page<Message> findConversationStarters(
            @Param("tenantId") Long tenantId,
            @Param("workspaceId") Long workspaceId,
            @Param("userId") Long userId,
            @Param("type") MessageType type,
            Pageable pageable);
}
