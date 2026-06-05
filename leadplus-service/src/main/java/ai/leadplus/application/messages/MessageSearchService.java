package ai.leadplus.application.messages;

import ai.leadplus.domain.messages.Message;
import ai.leadplus.domain.messages.MessageRepository;
import ai.leadplus.domain.messages.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageSearchService {

    private final MessageRepository messageRepository;

    public Page<Message> getConversationStarters(Long tenantId, Long workspaceId, Long userId, MessageType type, Pageable pageable) {
        return messageRepository.findConversationStarters(
                tenantId,
                workspaceId,
                userId,
                type,
                pageable);
    }
}
