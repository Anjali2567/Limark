package ai.leadplus.application.campaigns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignTotalDto {
    private long totalCompanies;
    private long totalContacts;
    private List<CampaignCompanyDto> campaignCompanies;
}
