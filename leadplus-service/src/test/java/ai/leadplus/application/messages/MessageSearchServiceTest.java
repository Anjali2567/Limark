package ai.leadplus.application.messages;

import ai.leadplus.domain.messages.Message;
import ai.leadplus.domain.messages.MessageRepository;
import ai.leadplus.domain.messages.MessageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageSearchServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageSearchService messageSearchService;

    @Test
    void getConversationStarters_shouldReturnEmptyPage_whenNoMessagesFound() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> page = new PageImpl<>(Collections.emptyList(), pageable, 0L);

        when(messageRepository.findConversationStarters(1L, 1L, 1L, MessageType.CAMPAIGN_AGENT, pageable)).thenReturn(page);

        Page<Message> result = messageSearchService.getConversationStarters(1L, 1L, 1L, MessageType.CAMPAIGN_AGENT, pageable);


        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(messageRepository).findConversationStarters(1L, 1L, 1L, MessageType.CAMPAIGN_AGENT, pageable);
    }

    @Test
    void getConversationStarters_shouldReturnMappedMessages_andTotal() {
        Pageable pageable = PageRequest.of(0, 10);

        Message message = Message.builder()
                .id(1L)
                .tenantId(1L)
                .workspaceId(1L)
                .userId(1L)
                .conversationId("conv-1")
                .type(MessageType.CAMPAIGN_AGENT)
                .build();

        Page<Message> page = new PageImpl<>(List.of(message), pageable, 1L);

        when(messageRepository.findConversationStarters(1L, 1L, 1L, MessageType.CAMPAIGN_AGENT, pageable)).thenReturn(page);

        Page<Message> result = messageSearchService.getConversationStarters(1L, 1L, 1L, MessageType.CAMPAIGN_AGENT, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().getFirst().getId());
        verify(messageRepository).findConversationStarters(1L, 1L, 1L, MessageType.CAMPAIGN_AGENT, pageable);
    }
}
