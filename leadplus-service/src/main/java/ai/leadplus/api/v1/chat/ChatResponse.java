package ai.leadplus.api.v1.chat;

import ai.leadplus.application.messages.MessageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private Long messageId;
    private String conversationId;
    private String response;

    public static ChatResponse fromDto(MessageDto messageDto) {
        return ChatResponse.builder()
                .messageId(messageDto.getId())
                .conversationId(messageDto.getConversationId())
                .response(messageDto.getResponse())
                .build();
    }
}
