package ai.leadplus.infrastructure.springai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringAiClient {

    private final OpenAiChatModel chatModel;

    public <T> T getChatCompletion(String userPrompt, String systemPrompt, Class<T> clazz, Object... functions) {
        return ChatClient
                .create(chatModel)
                .prompt()
                .tools(functions)
                .user(userPrompt)
                .system(systemPrompt)
                .call()
                .entity(clazz);
    }

    public <T> T getChatCompletion(String userPrompt, String systemPrompt, String conversationId, Class<T> clazz, Object... functions) {
        return ChatClient
                .create(chatModel)
                .prompt()
                .tools(functions)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(userPrompt)
                .system(systemPrompt)
                .call()
                .entity(clazz);
    }
}
