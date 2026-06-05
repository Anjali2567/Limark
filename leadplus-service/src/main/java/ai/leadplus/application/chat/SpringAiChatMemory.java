package ai.leadplus.application.chat;

import ai.leadplus.application.messages.MessageDto;
import ai.leadplus.application.messages.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Service
@Primary
@RequiredArgsConstructor
public class SpringAiChatMemory implements ChatMemoryRepository {

    private final MessageService messageService;

    @Override
    public List<String> findConversationIds() {
        return List.of();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        List<MessageDto> recentMessages = new ArrayList<>(messageService.getTop5MessagesByConversationId(conversationId));
        Collections.reverse(recentMessages);

        return recentMessages.stream()
                .flatMap(item -> Stream.of(
                        new UserMessage(item.getRequest()),
                        (Message) new AssistantMessage(item.getResponse())))
                .toList();
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {

    }

    @Override
    public void deleteByConversationId(String conversationId) {

    }
}
