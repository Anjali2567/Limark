package ai.leadplus.application.leads;

import ai.leadplus.domain.leadcompanies.LeadCompany;
import ai.leadplus.domain.leadcontacts.LeadContact;
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
public class LeadDto {
    private Long id;
    private Long leadCompanyId;
    private String firstName;
    private String lastName;
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
    private String companyTwitterUrl;
    private String companyFacebookUrl;
    private List<String> segments;
    private List<String> companySegments;
    private List<String> companyTenantIds;
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
    private List<String> companyTools;
    private List<String> companyServices;
    private String source;
    private TenantContactMetadataDto tenantMetadata;

    public static LeadDto fromEntities(LeadContact contact, LeadCompany company) {
        LeadDto lead = LeadDto.builder()
                .id(contact.getId())
                .leadCompanyId(contact.getLeadCompanyId())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .title(contact.getTitle())
                .seniority(contact.getSeniority())
                .department(contact.getDepartment())
                .email(contact.getEmail())
                .emailStatus(contact.getEmailStatus())
                .phoneE164(contact.getPhoneE164())
                .linkedinUrl(contact.getLinkedinUrl())
                .locationCity(contact.getLocationCity())
                .locationState(contact.getLocationState())
                .locationCountry(contact.getLocationCountry())
                .locationZip(contact.getLocationZip())
                .segments(contact.getSegments())
                .source(contact.getSource())
                .build();

        if (company != null) {
            lead.setCompanyName(company.getName());
            lead.setCompanyDomain(company.getDomain());
            lead.setCompanyDescription(company.getAccountSummary());
            lead.setCompanyLogoUrl(company.getLogoUrl());
            lead.setCompanyWebsiteUrl(company.getWebsiteUrl());
            lead.setCompanyLinkedinUrl(company.getLinkedinUrl());
            lead.setCompanyTwitterUrl(company.getTwitterUrl());
            lead.setCompanyFacebookUrl(company.getFacebookUrl());
            lead.setCompanySegments(company.getSegments());
            lead.setCompanyTenantIds(company.getTenantIds());
            lead.setCompanyIndustry(company.getIndustry());
            lead.setCompanyRevenueUsd(company.getRevenueUsd());
            lead.setCompanyEmployeeCount(company.getEmployeeCount());
            lead.setCompanyHqCity(company.getHqCity());
            lead.setCompanyHqState(company.getHqState());
            lead.setCompanyHqCountry(company.getHqCountry());
            lead.setCompanyPostalCode(company.getPostalCode());
            lead.setCompanySicCodes(company.getSicCodes());
            lead.setCompanyNaicsCodes(company.getNaicsCodes());
            lead.setCompanyKeywords(company.getKeywords());
            lead.setCompanyTechnologies(company.getTechnologies());
            lead.setCompanyTools(company.getScrapedTools());
            lead.setCompanyServices(company.getScrapedServices());
        }

        return lead;
    }
}
