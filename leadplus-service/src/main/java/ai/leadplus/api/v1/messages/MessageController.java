package ai.leadplus.api.v1.messages;

import ai.leadplus.api.common.UserValidator;
import ai.leadplus.application.messages.MessageDto;
import ai.leadplus.application.messages.MessageService;
import ai.leadplus.domain.messages.MessageType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/tenants/{tenantId}/workspaces/{workspaceId}/messages")
@RequiredArgsConstructor
@Tag(name = "Message API", description = "APIs for managing messages within a workspace")
public class MessageController {

    private final MessageService messageService;
    private final UserValidator userValidator;

    @Operation(summary = "Get Conversations")
    @GetMapping
    public ResponseEntity<Page<MessageResponse>> getConversations(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            Pageable pageable) {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[GET] Get conversations for user: {}", userId);
        Page<MessageDto> conversations = messageService.getMessagesByUserIdAndType(tenantId, workspaceId, Long.valueOf(userId), MessageType.CAMPAIGN_AGENT, pageable);
        Page<MessageResponse> messageResponses = conversations.map(MessageResponse::fromDto);
        log.info("[GET] Returning {} conversations for user: {}", conversations.getSize(), userId);
        return ResponseEntity.ok(messageResponses);
    }

    @Operation(summary = "Get Messages by Conversation ID")
    @GetMapping("/{conversationId}")
    public ResponseEntity<List<MessageResponse>> getMessagesByConversationId(
            @PathVariable Long tenantId,
            @PathVariable String workspaceId,
            @PathVariable String conversationId) {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[GET] Get Messages by Conversation ID: {}", conversationId);
        List<MessageDto> messages = messageService.getMessagesByConversationId(String.valueOf(tenantId), workspaceId, userId, conversationId);
        List<MessageResponse> messageResponses = messages.stream()
                .map(MessageResponse::fromDtoWithCounts)
                .toList();
        log.info("[GET] Returning {} messages for Conversation ID: {}", messageResponses.size(), conversationId);
        return ResponseEntity.ok(messageResponses);
    }
}
