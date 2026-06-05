package ai.leadplus.api.v1.campaigns;

import ai.leadplus.application.campaigncontacts.CampaignContactInfoDto;
import ai.leadplus.domain.campaigncontacts.CampaignContactStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignContactResponse {

    private Long id;
    private Long campaignId;
    private Long contactId;
    private String firstName;
    private String lastName;
    private String firstNameNormalized;
    private String lastNameNormalized;
    private String fullName;
    private String title;
    private String email;
    private String phoneE164;
    private String linkedinUrl;
    private boolean participating;
    private boolean zohoExisting;
    private int currentStep;
    private LocalDateTime lastSentAt;
    private LocalDateTime nextSendAt;
    private boolean replyReceived;
    private CampaignContactStatus status;
    private List<EmailDataResponse> emailData;
    private String bdName;
    private String bdEmail;

    public static CampaignContactResponse fromDto(CampaignContactInfoDto campaignContactInfoDto) {
        if (campaignContactInfoDto == null) {
            return null;
        }
        return CampaignContactResponse.builder()
                .id(campaignContactInfoDto.getId())
                .campaignId(campaignContactInfoDto.getCampaignId())
                .firstName(campaignContactInfoDto.getFirstName())
                .lastName(campaignContactInfoDto.getLastName())
                .firstNameNormalized(campaignContactInfoDto.getFirstNameNormalized())
                .lastNameNormalized(campaignContactInfoDto.getLastNameNormalized())
                .fullName(campaignContactInfoDto.getFullName())
                .title(campaignContactInfoDto.getTitle())
                .email(campaignContactInfoDto.getEmail())
                .phoneE164(campaignContactInfoDto.getPhoneE164())
                .linkedinUrl(campaignContactInfoDto.getLinkedinUrl())
                .contactId(campaignContactInfoDto.getContactId())
                .participating(campaignContactInfoDto.isParticipating())
                .zohoExisting(campaignContactInfoDto.isZohoExisting())
                .currentStep(campaignContactInfoDto.getCurrentStep())
                .lastSentAt(campaignContactInfoDto.getLastSentAt())
                .nextSendAt(campaignContactInfoDto.getNextSendAt())
                .replyReceived(campaignContactInfoDto.isReplyReceived())
                .status(campaignContactInfoDto.getStatus())
                .bdName(campaignContactInfoDto.getBdName())
                .bdEmail(campaignContactInfoDto.getBdEmail())
                .emailData(
                        CollectionUtils.isEmpty(campaignContactInfoDto.getEmailData())
                                ? List.of()
                                : campaignContactInfoDto.getEmailData()
                                .stream()
                                .map(EmailDataResponse::fromDto)
                                .toList()
                )
                .build();
    }
}
