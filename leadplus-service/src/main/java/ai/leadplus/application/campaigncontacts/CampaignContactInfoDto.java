package ai.leadplus.application.campaigncontacts;

import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.domain.campaigncontacts.CampaignContact;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.util.CollectionUtils;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignContactInfoDto extends CampaignContactDto {

    private Long leadCompanyId;
    private String firstName;
    private String lastName;
    private String firstNameNormalized;
    private String lastNameNormalized;
    private String fullName;
    private String title;
    private String email;
    private String phoneE164;
    private String linkedinUrl;
    private String locationState;
    private String locationCountry;
    private boolean zohoExisting;
    private String bdName;
    private String bdEmail;
    private String bdPhone;
    private String isrName;
    private String isrEmail;
    private String isrPhone;

    public static CampaignContactInfoDto fromEntity(CampaignContact campaignContact, LeadContactDto leadContactDto) {
        return fromEntity(campaignContact, leadContactDto, false);
    }

    public static CampaignContactInfoDto fromEntity(CampaignContact campaignContact, LeadContactDto leadContactDto, boolean zohoExisting) {
        return CampaignContactInfoDto.builder()
                .id(campaignContact.getId())
                .campaignId(campaignContact.getCampaignId())
                .leadCompanyId(leadContactDto.getLeadCompanyId())
                .firstName(leadContactDto.getFirstName())
                .lastName(leadContactDto.getLastName())
                .firstNameNormalized(leadContactDto.getFirstNameNormalized())
                .lastNameNormalized(leadContactDto.getLastNameNormalized())
                .fullName(leadContactDto.getFullName())
                .title(leadContactDto.getTitle())
                .email(leadContactDto.getEmail())
                .phoneE164(leadContactDto.getPhoneE164())
                .linkedinUrl(leadContactDto.getLinkedinUrl())
                .locationState(leadContactDto.getLocationState())
                .locationCountry(leadContactDto.getLocationCountry())
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
                .zohoExisting(zohoExisting)
                .build();
    }

    public static CampaignContactInfoDto fromChatMemory(Long campaignId, LeadContactDto leadContactDto, boolean zohoExisting) {
        return CampaignContactInfoDto.builder()
                .campaignId(campaignId)
                .leadCompanyId(leadContactDto.getLeadCompanyId())
                .contactId(leadContactDto.getId())
                .firstName(leadContactDto.getFirstName())
                .lastName(leadContactDto.getLastName())
                .firstNameNormalized(leadContactDto.getFirstNameNormalized())
                .lastNameNormalized(leadContactDto.getLastNameNormalized())
                .fullName(leadContactDto.getFullName())
                .title(leadContactDto.getTitle())
                .email(leadContactDto.getEmail())
                .phoneE164(leadContactDto.getPhoneE164())
                .linkedinUrl(leadContactDto.getLinkedinUrl())
                .zohoExisting(zohoExisting)
                .build();
    }
}
