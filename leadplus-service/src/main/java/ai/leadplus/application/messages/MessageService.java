package ai.leadplus.application.messages;

import ai.leadplus.application.exception.UnauthorizedException;
import ai.leadplus.domain.messages.Message;
import ai.leadplus.domain.messages.MessageRepository;
import ai.leadplus.domain.messages.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageSearchService messageSearchService;

    public List<MessageDto> getTop5MessagesByConversationId(String conversationId) {
        List<Message> messageList = messageRepository.findTop5ByConversationIdOrderByCreatedAtDesc(conversationId);
        return messageList.stream()
                .map(MessageDto::fromEntity)
                .toList();
    }

    public MessageDto createMessage(MessageDto messageDto, MessageType messageType) {
        Message message = messageDto.toEntity();
        message.setType(messageType);
        if (!StringUtils.hasText(messageDto.getConversationId())) {
            Message savedMessage = messageRepository.save(message);
            savedMessage.setConversationId(String.valueOf(savedMessage.getId()));
            return MessageDto.fromEntity(messageRepository.save(savedMessage));
        }
        return MessageDto.fromEntity(messageRepository.save(message));
    }

    public Page<MessageDto> getMessagesByUserIdAndType(Long tenantId, Long workspaceId, Long userId, MessageType messageType, Pageable pageable) {
        Page<Message> messagePage = messageSearchService.getConversationStarters(tenantId, workspaceId, userId, messageType, pageable);
        return messagePage.map(MessageDto::fromEntity);
    }

    public List<MessageDto> getMessagesByConversationId(String tenantId, String workspaceId, String userId, String conversationId) {
        List<Message> messageList = messageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId);

        if (CollectionUtils.isEmpty(messageList)) {
            return  List.of();
        }

        Message firstMessage = messageList.getFirst();
        boolean authorized = Objects.equals(firstMessage.getUserId(), parseLongSafe(userId))
                && Objects.equals(firstMessage.getTenantId(), parseLongSafe(tenantId))
                && Objects.equals(firstMessage.getWorkspaceId(), parseLongSafe(workspaceId));
        if (!authorized) {
            throw new UnauthorizedException("User is not authorized to access messages in this conversation.");
        }

        return messageList.stream()
                .map(MessageDto::fromEntity)
                .toList();
    }

    public List<MessageDto> getLeadChatMessagesByConversationId(Long tenantId, Long userId, Long conversationId) {
        List<Message> messageList = messageRepository.findAllByConversationIdAndTypeOrderByCreatedAtAsc(
                String.valueOf(conversationId),
                MessageType.LEAD_CHAT_ASSISTANT
        );

        if (CollectionUtils.isEmpty(messageList)) {
            return List.of();
        }

        Message firstMessage = messageList.getFirst();
        boolean authorized = Objects.equals(firstMessage.getUserId(), userId)
                && Objects.equals(firstMessage.getTenantId(), tenantId);
        if (!authorized) {
            throw new UnauthorizedException("User is not authorized to access lead chat messages in this conversation.");
        }

        return messageList.stream()
                .map(MessageDto::fromEntity)
                .toList();
    }

    private Long parseLongSafe(String value) {
        try {
            return value != null ? Long.parseLong(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
