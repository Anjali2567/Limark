package ai.leadplus.api.v1.campaigns.campaignemails;

import ai.leadplus.application.campaignemails.CampaignEmailDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignEmailRequest {

    private String subject;
    private String bodyTemplate;
    private int delayDays;
    private List<String> attachmentIds;

    public CampaignEmailDto toDto() {
        return CampaignEmailDto.builder()
                .subject(subject)
                .bodyTemplate(bodyTemplate)
                .delayDays(delayDays)
                .attachmentIds(attachmentIds)
                .build();
    }
}
