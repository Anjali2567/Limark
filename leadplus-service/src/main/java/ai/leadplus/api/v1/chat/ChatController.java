package ai.leadplus.api.v1.chat;

import ai.leadplus.api.common.UserValidator;
import ai.leadplus.application.chat.ChatService;
import ai.leadplus.application.messages.MessageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Customer Chat Assistant Controller", description = "APIs for customer chat assistant interactions")
public class ChatController {

    private final ChatService chatService;
    private final UserValidator userValidator;

    @Operation(summary = "Get Chat Reply from Customer Chat Assistant")
    @PostMapping
    public ResponseEntity<ChatResponse> getChatReply(@RequestBody ChatRequest chatRequest) {
        log.info("Received chat request: {}", chatRequest.getRequest());
        String userId = userValidator.getAuthenticatedUserId();
        MessageDto messageDto = chatService.getReply(chatRequest.toDto(Long.valueOf(userId)));
        ChatResponse chatResponse = ChatResponse.fromDto(messageDto);
        log.info("Response generated successfully for conversationId: {}", chatResponse.getConversationId());
        return ResponseEntity.ok(chatResponse);
    }

}
