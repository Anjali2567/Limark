package ai.leadplus.application.campaigns;

import ai.leadplus.domain.campaigns.Campaign;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignListDto extends CampaignDto {
    private Integer totalCompanies;
    private Integer totalContacts;
    private Long sentEmails;
    private Long openedEmails;
    private Double progress;
    private Integer emailSteps;

    public static CampaignListDto fromEntity(Campaign campaign, Integer totalCompanies, Integer totalContacts, Long sentEmails, Long openedEmails, Double progress, Integer emailSteps) {
        return baseBuilder(CampaignListDto.builder(), campaign)
                .totalCompanies(totalCompanies)
                .totalContacts(totalContacts)
                .sentEmails(sentEmails)
                .openedEmails(openedEmails)
                .emailSteps(emailSteps)
                .progress(progress)
                .build();
    }

}
