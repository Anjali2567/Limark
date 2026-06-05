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
public class CampaignGeneratorRequest {

    private String conversationId;
    private String request;

    public MessageDto toDto() {
        return MessageDto.builder()
                .conversationId(conversationId)
                .request(request)
                .build();
    }
}
