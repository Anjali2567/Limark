package ai.leadplus.application.messages;

import ai.leadplus.application.common.TargetingCriteriaDto;
import ai.leadplus.domain.messages.Message;
import ai.leadplus.domain.messages.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {

    private Long id;
    private Long tenantId;
    private Long workspaceId;
    private String conversationId;
    private Long userId;
    private MessageType type;
    private String request;
    private String response;
    private Long campaignId;
    private TargetingCriteriaDto campaignState;
    private boolean searchPerformed;
    private LocalDateTime createdAt;

    public static MessageDto fromEntity(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .tenantId(message.getTenantId())
                .workspaceId(message.getWorkspaceId())
                .conversationId(message.getConversationId())
                .userId(message.getUserId())
                .type(message.getType())
                .request(message.getRequest())
                .response(message.getResponse())
                .campaignId(message.getCampaignId())
                .campaignState(message.getTargetingCriteria() != null ? TargetingCriteriaDto.fromEntity(message.getTargetingCriteria()) : null)
                .searchPerformed(message.isSearchPerformed())
                .createdAt(message.getCreatedAt())
                .build();
    }

    public Message toEntity() {
        return Message.builder()
                .id(id)
                .tenantId(tenantId)
                .workspaceId(workspaceId)
                .conversationId(conversationId)
                .userId(userId)
                .type(type)
                .request(request)
                .response(response)
                .campaignId(campaignId)
                .targetingCriteria(campaignState != null ? campaignState.toEntity() : null)
                .searchPerformed(searchPerformed)
                .createdAt(createdAt)
                .build();
    }
}
