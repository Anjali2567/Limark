package ai.leadplus.api.v1.campaigns;

import ai.leadplus.application.common.TargetingCriteriaDto;
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
public class CampaignAgentResponse {

    private Long userId;
    private Long messageId;
    private String conversationId;
    private String response;
    private Long campaignId;
    private TargetingCriteriaDto campaignState;
    private boolean searchPerformed;
    private LocalDateTime createdAt;

    public static CampaignAgentResponse fromDto(MessageDto messageDto) {
        return CampaignAgentResponse.builder()
                .userId(messageDto.getUserId())
                .messageId(messageDto.getId())
                .conversationId(messageDto.getConversationId())
                .response(messageDto.getResponse())
                .campaignId(messageDto.getCampaignId())
                .campaignState(messageDto.getCampaignState())
                .searchPerformed(messageDto.isSearchPerformed())
                .createdAt(messageDto.getCreatedAt())
                .build();
    }
}
