package ai.leadplus.api.v1.leads;

import ai.leadplus.application.leads.CurrentCampaignDto;
import ai.leadplus.application.leads.LeadDto;
import ai.leadplus.application.leads.TenantContactMetadataDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadResponse {
    private Long id;
    private Long leadCompanyId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String title;
    private String seniority;
    private String department;
    private String email;
    private String emailStatus;
    private String phoneE164;
    private String linkedinUrl;
    private String locationCity;
    private String locationState;
    private String locationCountry;
    private String locationZip;
    private List<CurrentCampaignDto> currentCampaigns;
    private boolean zohoExisting;
    private boolean hubspotExisting;
    private String companyName;
    private String companyDomain;
    private String companyDescription;
    private String companyLogoUrl;
    private String companyWebsiteUrl;
    private String companyLinkedinUrl;
    private List<String> companySegments;
    private String companyIndustry;
    private BigDecimal companyRevenueUsd;
    private String companyEmployeeCount;
    private String companyHqCity;
    private String companyHqState;
    private String companyHqCountry;
    private String companyPostalCode;
    private List<String> companySicCodes;
    private List<String> companyNaicsCodes;
    private List<String> companyKeywords;
    private List<String> companyTechnologies;
    private String source;
    private TenantContactMetadataDto tenantMetadata;

    public static LeadResponse fromDto(LeadDto leadDto) {
        return LeadResponse.builder()
                .id(leadDto.getId())
                .leadCompanyId(leadDto.getLeadCompanyId())
                .firstName(leadDto.getFirstName())
                .lastName(leadDto.getLastName())
                .title(leadDto.getTitle())
                .seniority(leadDto.getSeniority())
                .department(leadDto.getDepartment())
                .email(leadDto.getEmail())
                .emailStatus(leadDto.getEmailStatus())
                .phoneE164(leadDto.getPhoneE164())
                .linkedinUrl(leadDto.getLinkedinUrl())
                .locationCity(leadDto.getLocationCity())
                .locationState(leadDto.getLocationState())
                .locationCountry(leadDto.getLocationCountry())
                .locationZip(leadDto.getLocationZip())
                .currentCampaigns(leadDto.getCurrentCampaigns())
                .zohoExisting(leadDto.isZohoExisting())
                .hubspotExisting(leadDto.isHubspotExisting())
                .companyName(leadDto.getCompanyName())
                .companyDomain(leadDto.getCompanyDomain())
                .companyDescription(leadDto.getCompanyDescription())
                .companyLogoUrl(leadDto.getCompanyLogoUrl())
                .companyWebsiteUrl(leadDto.getCompanyWebsiteUrl())
                .companyLinkedinUrl(leadDto.getCompanyLinkedinUrl())
                .companySegments(leadDto.getCompanySegments())
                .companyIndustry(leadDto.getCompanyIndustry())
                .companyRevenueUsd(leadDto.getCompanyRevenueUsd())
                .companyEmployeeCount(leadDto.getCompanyEmployeeCount())
                .companyHqCity(leadDto.getCompanyHqCity())
                .companyHqState(leadDto.getCompanyHqState())
                .companyHqCountry(leadDto.getCompanyHqCountry())
                .companyPostalCode(leadDto.getCompanyPostalCode())
                .companySicCodes(leadDto.getCompanySicCodes())
                .companyNaicsCodes(leadDto.getCompanyNaicsCodes())
                .companyKeywords(leadDto.getCompanyKeywords())
                .companyTechnologies(leadDto.getCompanyTechnologies())
                .source(leadDto.getSource())
                .tenantMetadata(leadDto.getTenantMetadata())
                .build();
    }
}
