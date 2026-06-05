package ai.leadplus.application.campaignemails;

import ai.leadplus.domain.campaignemails.CampaignEmail;
import ai.leadplus.domain.campaignemails.CampaignEmailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignEmailDto {

    private Long id;
    private Long campaignId;
    private int stepNumber;
    private String subject;
    private String bodyTemplate;
    private int delayDays;
    private List<String> attachmentIds;
    private CampaignEmailStatus status;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static CampaignEmailDto fromEntity(CampaignEmail campaignEmail) {
        if (campaignEmail == null) {
            return null;
        }
        return CampaignEmailDto.builder()
                .id(campaignEmail.getId())
                .campaignId(campaignEmail.getCampaignId())
                .stepNumber(campaignEmail.getStepNumber())
                .subject(campaignEmail.getSubject())
                .bodyTemplate(campaignEmail.getBodyTemplate())
                .delayDays(campaignEmail.getDelayDays())
                .attachmentIds(campaignEmail.getAttachmentIds())
                .status(campaignEmail.getStatus())
                .updatedBy(campaignEmail.getUpdatedBy())
                .updatedAt(campaignEmail.getUpdatedAt())
                .build();
    }

    public CampaignEmail toEntity() {
        return CampaignEmail.builder()
                .id(id)
                .campaignId(campaignId)
                .stepNumber(stepNumber)
                .subject(subject)
                .bodyTemplate(bodyTemplate)
                .delayDays(delayDays)
                .attachmentIds(attachmentIds)
                .status(status)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }

    public String toAiPromptString() {
        return "Step " + stepNumber + ":\n" +
               "Subject: " + subject + "\n" +
               "Body Template: " + bodyTemplate + "\n" +
               "Delay Days: " + delayDays + "\n";
    }
}
