package ai.leadplus.application.campaigncontacts;

import ai.leadplus.domain.campaigncontacts.CampaignContact;
import ai.leadplus.domain.campaigncontacts.CampaignContactStatus;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.CollectionUtils;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignContactDto {

    private Long id;
    private Long campaignId;
    private Long contactId;
    private boolean participating;
    private int currentStep;
    private LocalDateTime lastSentAt;
    private LocalDateTime nextSendAt;
    private boolean replyReceived;
    private List<EmailDataDto> emailData;
    private CampaignContactStatus status;
    private LocalDateTime updatedAt;

    public static CampaignContactDto fromEntity(CampaignContact campaignContact) {
        return CampaignContactDto.builder()
                .id(campaignContact.getId())
                .campaignId(campaignContact.getCampaignId())
                .contactId(campaignContact.getContactId())
                .participating(campaignContact.isParticipating())
                .currentStep(campaignContact.getCurrentStep())
                .lastSentAt(campaignContact.getLastSentAt())
                .nextSendAt(campaignContact.getNextSendAt())
                .replyReceived(campaignContact.isReplyReceived())
                .emailData(CollectionUtils.isEmpty(campaignContact.getEmailData()) ? null :
                        campaignContact.getEmailData().stream()
                                .map(EmailDataDto::fromEntity)
                                .toList())
                .status(campaignContact.getStatus())
                .updatedAt(campaignContact.getUpdatedAt())
                .build();
    }

    public CampaignContact toEntity() {
        return CampaignContact.builder()
                .id(id)
                .campaignId(campaignId)
                .contactId(contactId)
                .participating(participating)
                .currentStep(currentStep)
                .lastSentAt(lastSentAt)
                .nextSendAt(nextSendAt)
                .replyReceived(replyReceived)
                .emailData(CollectionUtils.isEmpty(emailData) ? null :
                        emailData.stream()
                                .map(EmailDataDto::toEntity)
                                .toList())
                .status(status)
                .updatedAt(updatedAt)
                .build();
    }
}
