package ai.leadplus.api.v1.campaigns.campaignemails;

import ai.leadplus.application.campaignemails.CampaignEmailDto;
import ai.leadplus.domain.campaignemails.CampaignEmailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignEmailResponse {

    private Long id;
    private Long campaignId;
    private int stepNumber;
    private String subject;
    private String bodyTemplate;
    private int delayDays;
    private List<String> attachmentIds;
    private CampaignEmailStatus status;

    public static CampaignEmailResponse fromDto(CampaignEmailDto dto) {
        return CampaignEmailResponse.builder()
                .id(dto.getId())
                .campaignId(dto.getCampaignId())
                .stepNumber(dto.getStepNumber())
                .subject(dto.getSubject())
                .bodyTemplate(dto.getBodyTemplate())
                .delayDays(dto.getDelayDays())
                .attachmentIds(dto.getAttachmentIds())
                .status(dto.getStatus())
                .build();
    }
}
