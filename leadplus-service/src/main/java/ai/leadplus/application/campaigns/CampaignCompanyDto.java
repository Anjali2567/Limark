package ai.leadplus.application.campaigns;

import ai.leadplus.application.campaigncontacts.CampaignContactInfoDto;
import ai.leadplus.application.leadcompany.LeadCompanyDto;
import ai.leadplus.domain.leadcompanies.EmployeeRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignCompanyDto {

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
    private List<CampaignContactInfoDto> campaignContacts;

    public static CampaignCompanyDto fromEntity(LeadCompanyDto leadCompanyDto, List<CampaignContactInfoDto> campaignContactList) {
        if (leadCompanyDto == null) {
            return null;
        }
        return CampaignCompanyDto.builder()
                .id(leadCompanyDto.getId())
                .name(leadCompanyDto.getName())
                .domain(leadCompanyDto.getDomain())
                .websiteUrl(leadCompanyDto.getWebsiteUrl())
                .industry(leadCompanyDto.getIndustry())
                .employeeCount(leadCompanyDto.getEmployeeCount())
                .employeeRange(leadCompanyDto.getEmployeeRange())
                .hqCity(leadCompanyDto.getHqCity())
                .hqState(leadCompanyDto.getHqState())
                .hqCountry(leadCompanyDto.getHqCountry())
                .territory(leadCompanyDto.getTerritory())
                .region(leadCompanyDto.getRegion())
                .accountSummary(leadCompanyDto.getAccountSummary())
                .segments(leadCompanyDto.getSegments())
                .campaignContacts(campaignContactList)
                .build();
    }
}
