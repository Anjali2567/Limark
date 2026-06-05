package ai.leadplus.api.v1.leads;

import ai.leadplus.application.messages.MessageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadChatMessageResponse {
    private Long messageId;
    private Long conversationId;
    private String request;
    private String response;
    private LocalDateTime createdAt;

    public static LeadChatMessageResponse fromDto(MessageDto messageDto) {
        return LeadChatMessageResponse.builder()
                .messageId(messageDto.getId())
                .conversationId(parseConversationId(messageDto.getConversationId()))
                .request(messageDto.getRequest())
                .response(messageDto.getResponse())
                .createdAt(messageDto.getCreatedAt())
                .build();
    }

    private static Long parseConversationId(String conversationId) {
        if (conversationId == null) {
            return null;
        }
        try {
            return Long.valueOf(conversationId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
