package ai.leadplus.api.v1.campaigns;

import ai.leadplus.application.campaigns.CampaignCompanyDto;
import ai.leadplus.domain.leadcompanies.EmployeeRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignCompanyResponse {

    private Long id;
    private String name;
    private String domain;
    private String websiteUrl;
    private String industry;
    private String employeeCount;
    private EmployeeRange employeeRange;
    private String hqCity;
    private String hqState;
    private String hqCountry;
    private String territory;
    private String region;
    private String accountSummary;
    private List<String> segments;
    private List<CampaignContactResponse> campaignContacts;

    public static CampaignCompanyResponse fromDto(CampaignCompanyDto campaignCompanyDto) {
        if (campaignCompanyDto == null) {
            return null;
        }
        return CampaignCompanyResponse.builder()
                .id(campaignCompanyDto.getId())
                .name(campaignCompanyDto.getName())
                .domain(campaignCompanyDto.getDomain())
                .websiteUrl(campaignCompanyDto.getWebsiteUrl())
                .industry(campaignCompanyDto.getIndustry())
                .employeeCount(campaignCompanyDto.getEmployeeCount())
                .employeeRange(campaignCompanyDto.getEmployeeRange())
                .hqCity(campaignCompanyDto.getHqCity())
                .hqState(campaignCompanyDto.getHqState())
                .hqCountry(campaignCompanyDto.getHqCountry())
                .territory(campaignCompanyDto.getTerritory())
                .region(campaignCompanyDto.getRegion())
                .accountSummary(campaignCompanyDto.getAccountSummary())
                .segments(campaignCompanyDto.getSegments())
                .campaignContacts(CollectionUtils.isEmpty(campaignCompanyDto.getCampaignContacts()) ?
                        null :
                        campaignCompanyDto.getCampaignContacts().stream()
                                .map(CampaignContactResponse::fromDto)
                                .toList())
                .build();
    }
}
