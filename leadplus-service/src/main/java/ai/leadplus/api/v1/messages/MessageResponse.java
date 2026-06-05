package ai.leadplus.api.v1.messages;

import ai.leadplus.application.messages.MessageDto;
import ai.leadplus.domain.messages.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private Long id;
    private String conversationId;
    private Long userId;
    private MessageType type;
    private String request;
    private String response;
    private LocalDateTime createdAt;
    private TargetingCriteriaCountResponse targetingCriteriaCount;
    private boolean searchPerformed;

    public static MessageResponse fromDto(MessageDto messageDto) {
        return MessageResponse.builder()
                .id(messageDto.getId())
                .conversationId(messageDto.getConversationId())
                .userId(messageDto.getUserId())
                .type(messageDto.getType())
                .request(messageDto.getRequest())
                .response(messageDto.getResponse())
                .createdAt(messageDto.getCreatedAt())
                .build();
    }

    public static MessageResponse fromDtoWithCounts(MessageDto messageDto) {
        TargetingCriteriaCountResponse counts = messageDto.getCampaignState() != null
                ? TargetingCriteriaCountResponse.fromDto(messageDto.getCampaignState())
                : null;
        MessageResponse response = fromDto(messageDto);
        response.setTargetingCriteriaCount(counts);
        response.setSearchPerformed(messageDto.isSearchPerformed());
        return response;
    }
}
