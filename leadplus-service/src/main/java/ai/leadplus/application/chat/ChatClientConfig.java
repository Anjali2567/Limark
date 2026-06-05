package ai.leadplus.application.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChatClientConfig {

    private final SpringAiChatMemory springAiChatMemory;
    private final OpenAiChatModel chatModel;

    @Bean
    public ChatClient chatClient() {
        org.springframework.ai.chat.memory.ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(this.springAiChatMemory)
                .build();

        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
