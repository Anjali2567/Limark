package ai.leadplus.application.messages;

import ai.leadplus.application.exception.UnauthorizedException;
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
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessageSearchService messageSearchService;

    @InjectMocks
    private MessageService messageService;

    @Test
    void getTop5MessagesByConversationId_shouldCallRepository_andReturnEmptyList() {
        String conversationId = "conv-1";
        when(messageRepository.findTop5ByConversationIdOrderByCreatedAtDesc(conversationId))
                .thenReturn(Collections.emptyList());

        List<MessageDto> result = messageService.getTop5MessagesByConversationId(conversationId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(messageRepository).findTop5ByConversationIdOrderByCreatedAtDesc(conversationId);
    }

    @Test
    void createMessage_shouldSaveOnce_whenConversationIdProvided() {
        MessageDto messageDto = mock(MessageDto.class);
        Message messageEntity = spy(new Message());
        when(messageDto.getConversationId()).thenReturn("conv-existing");
        when(messageDto.toEntity()).thenReturn(messageEntity);
        when(messageRepository.save(any(Message.class))).thenReturn(messageEntity);

        MessageDto result = messageService.createMessage(messageDto, MessageType.CAMPAIGN_AGENT);

        verify(messageRepository, times(1)).save(messageEntity);
        assertEquals(MessageType.CAMPAIGN_AGENT, messageEntity.getType());
        assertNotNull(result);
    }

    @Test
    void createMessage_shouldSaveTwice_andSetConversationId_whenConversationIdNotProvided() {
        MessageDto messageDto = mock(MessageDto.class);
        Message messageEntity = spy(new Message());
        when(messageDto.getConversationId()).thenReturn(null);
        when(messageDto.toEntity()).thenReturn(messageEntity);

        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            if (m.getId() == null) {
                m.setId(1L);
            }
            return m;
        });

        MessageDto result = messageService.createMessage(messageDto, MessageType.CAMPAIGN_AGENT);

        verify(messageRepository, times(2)).save(any(Message.class));
        assertEquals("1", messageEntity.getConversationId());
        assertEquals(MessageType.CAMPAIGN_AGENT, messageEntity.getType());
        assertNotNull(result);
    }

    @Test
    void getMessagesByConversationId_shouldThrowUnauthorized_whenUserDoesNotMatchFirstMessageUser() {
        String conversationId = "conv-2";
        String requesterUserId = "user-req";
        String tenantId = "tenant-a";
        String workspaceId = "ws-a";

        Message firstMessage = mock(Message.class);
        when(firstMessage.getUserId()).thenReturn(0L);

        List<Message> messages = List.of(firstMessage);

        when(messageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId))
                .thenReturn(messages);

        assertThrows(UnauthorizedException.class,
                () -> messageService.getMessagesByConversationId(tenantId, workspaceId, requesterUserId, conversationId));

        verify(messageRepository).findAllByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Test
    void getMessagesByConversationId_shouldReturnList_whenUserMatchesFirstMessageUser() {
        String conversationId = "conv-3";
        Long requesterUserId = 1L;
        Long tenantId = 1L;
        Long workspaceId = 1L;

        Message firstMessage = mock(Message.class);
        when(firstMessage.getUserId()).thenReturn(requesterUserId); // Requester user ID matches
        when(firstMessage.getTenantId()).thenReturn(tenantId);
        when(firstMessage.getWorkspaceId()).thenReturn(workspaceId);

        List<Message> messages = List.of(firstMessage);

        when(messageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId))
                .thenReturn(messages);

        // Fix: Convert String arguments to Long to match the type of requesterUserId, tenantId, and workspaceId.
        List<MessageDto> result = messageService.getMessagesByConversationId(
                "1", "1", "1", conversationId
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(messageRepository).findAllByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Test
    void getMessagesByUserIdAndType_shouldDelegateToSearchService_andReturnPage() {
        Long tenantId = 1L;
        Long workspaceId = 1L;
        Long userId = 1L;
        MessageType type = MessageType.CAMPAIGN_AGENT;
        Pageable pageable = Pageable.unpaged();

        Page<Message> emptyPage = new PageImpl<>(Collections.emptyList());
        when(messageSearchService.getConversationStarters(tenantId, workspaceId, userId, type, pageable))
                .thenReturn(emptyPage);

        Page<MessageDto> result = messageService.getMessagesByUserIdAndType(tenantId, workspaceId, userId, type, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(messageSearchService).getConversationStarters(tenantId, workspaceId, userId, type, pageable);
    }
}
