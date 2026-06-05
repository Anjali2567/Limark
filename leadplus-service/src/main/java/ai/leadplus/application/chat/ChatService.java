package ai.leadplus.application.chat;

import ai.leadplus.application.common.utils.FileReader;
import ai.leadplus.application.messages.MessageDto;
import ai.leadplus.application.messages.MessageService;
import ai.leadplus.domain.messages.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final MessageService messageService;

    private static final String SYSTEM_PROMPT = FileReader.readFileContentFromClasspath("prompts/customer-chat-assistant.md");

    public MessageDto getReply(MessageDto messageDto) {
        ChatCompletion completion = getChatCompletion(messageDto);
        messageDto.setResponse(completion.getResponse());
        return messageService.createMessage(messageDto, MessageType.CUSTOMER_CHAT_ASSISTANT);
    }

    private ChatCompletion getChatCompletion(MessageDto messageDto) {
        String conversationId = StringUtils.hasText(messageDto.getConversationId()) ?
                messageDto.getConversationId() : "-";
        return chatClient
                .prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(messageDto.getRequest())
                .system(SYSTEM_PROMPT)
                .call()
                .entity(ChatCompletion.class);
    }
}
