package ai.leadplus.api.v1.chat;

import ai.leadplus.application.messages.MessageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {

    private String conversationId;
    private String request;

    public MessageDto toDto(Long userId) {
        return MessageDto.builder()
                .conversationId(conversationId)
                .userId(userId)
                .request(request)
                .build();
    }
}