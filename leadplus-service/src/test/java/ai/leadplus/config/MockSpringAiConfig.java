package ai.leadplus.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

/**
 * Test configuration providing mock Spring AI beans for integration tests.
 * Allows services depending on ChatClient and ChatModel to work during testing
 * without requiring complex OpenAI API configuration.
 */
@TestConfiguration
@Profile("staging-test")
public class MockSpringAiConfig {

    /**
     * Provide a mock ChatModel for testing.
     * Services that use ChatModel can proceed without actual API calls.
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(ChatModel.class)
    public ChatModel mockChatModel() {
        return mock(ChatModel.class);
    }

    /**
     * Provide a mock ChatClient for testing.
     * Services like CampaignAgentService depend on ChatClient; this prevents
     * context initialization failures when the real OpenAI auto-config doesn't provide it.
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(ChatClient.class)
    public ChatClient mockChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
