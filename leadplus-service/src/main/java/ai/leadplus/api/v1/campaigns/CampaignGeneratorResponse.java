package ai.leadplus.api.v1.campaigns;

import ai.leadplus.application.messages.MessageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignGeneratorResponse {
    private String conversationId;
    private Long campaignId;

    public static CampaignGeneratorResponse fromDto(MessageDto messageDto) {
        return CampaignGeneratorResponse.builder()
                .campaignId(messageDto.getCampaignId())
                .conversationId(messageDto.getConversationId())
                .build();
    }
}
