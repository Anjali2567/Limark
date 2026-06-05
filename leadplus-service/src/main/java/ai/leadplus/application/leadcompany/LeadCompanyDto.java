package ai.leadplus.application.leadcompany;

import ai.leadplus.domain.leadcompanies.EmployeeRange;
import ai.leadplus.domain.leadcompanies.LeadCompany;
import ai.leadplus.domain.leadcompanies.LeadCompanyStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LeadCompanyDto {

    private Long id;
    private String zohoAccountId;
    private String name;
    private String domain;
    private String websiteUrl;
    private String logoUrl;
    private String linkedinUrl;
    private String twitterUrl;
    private String facebookUrl;
    private String phoneNumber;
    private String industry;
    private BigDecimal revenueUsd;
    private Long revenueUsdAmount;
    private String employeeCount;
    private EmployeeRange employeeRange;
    private String hqCity;
    private String hqState;
    private String hqCountry;
    private String postalCode;
    private String territory;
    private String region;
    private String icpTag;
    private List<String> sicCodes;
    private List<String> naicsCodes;
    private List<String> keywords;
    private List<String> technologies;
    private List<String> scrapedTechnologies;
    private List<String> scrapedTools;
    private List<String> scrapedServices;
    private String publiclyTradedSymbol;
    private int score;
    private String accountSummary;
    private Long salespersonId;
    private String salespersonName;
    private LocalDateTime salespersonAssignAt;
    private String source;
    private List<String> segments;
    private List<String> tenantIds;
    private boolean isTargetAccount;
    private LeadCompanyStatus leadCompanyStatus;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LeadCompanyDto toDto(LeadCompany leadCompany) {
        if (leadCompany == null) {
            return null;
        }
        return baseBuilder(LeadCompanyDto.builder(), leadCompany)
                .build();
    }

    @SuppressWarnings("unchecked")
    protected static <T extends LeadCompanyDtoBuilder<?, ?>> T baseBuilder(T builder, LeadCompany leadCompany) {
        if (leadCompany == null) {
            return null;
        }
        return (T) builder
                .id(leadCompany.getId())
                .zohoAccountId(leadCompany.getZohoAccountId())
                .name(leadCompany.getName())
                .domain(leadCompany.getDomain())
                .websiteUrl(leadCompany.getWebsiteUrl())
                .logoUrl(leadCompany.getLogoUrl())
                .linkedinUrl(leadCompany.getLinkedinUrl())
                .twitterUrl(leadCompany.getTwitterUrl())
                .facebookUrl(leadCompany.getFacebookUrl())
                .phoneNumber(leadCompany.getPhoneNumber())
                .industry(leadCompany.getIndustry())
                .revenueUsd(leadCompany.getRevenueUsd())
                .revenueUsdAmount(leadCompany.getRevenueUsdAmount())
                .employeeCount(leadCompany.getEmployeeCount())
                .employeeRange(leadCompany.getEmployeeRange())
                .hqCity(leadCompany.getHqCity())
                .hqState(leadCompany.getHqState())
                .hqCountry(leadCompany.getHqCountry())
                .postalCode(leadCompany.getPostalCode())
                .territory(leadCompany.getTerritory())
                .region(leadCompany.getRegion())
                .icpTag(leadCompany.getIcpTag())
                .sicCodes(leadCompany.getSicCodes())
                .naicsCodes(leadCompany.getNaicsCodes())
                .keywords(leadCompany.getKeywords())
                .technologies(leadCompany.getTechnologies())
                .scrapedTechnologies(leadCompany.getScrapedTechnologies())
                .scrapedTools(leadCompany.getScrapedTools())
                .scrapedServices(leadCompany.getScrapedServices())
                .publiclyTradedSymbol(leadCompany.getPubliclyTradedSymbol())
                .score(leadCompany.getScore())
                .accountSummary(leadCompany.getAccountSummary())
                .salespersonId(leadCompany.getSalespersonId())
                .salespersonName(leadCompany.getSalespersonName())
                .salespersonAssignAt(leadCompany.getSalespersonAssignAt())
                .source(leadCompany.getSource())
                .segments(leadCompany.getSegments())
                .tenantIds(leadCompany.getTenantIds())
                .isTargetAccount(leadCompany.isTargetAccount())
                .leadCompanyStatus(leadCompany.getLeadCompanyStatus())
                .active(leadCompany.isActive())
                .createdAt(leadCompany.getCreatedAt())
                .updatedAt(leadCompany.getUpdatedAt());
    }

    public LeadCompany toEntity() {
        return LeadCompany.builder()
                .id(id)
                .zohoAccountId(zohoAccountId)
                .name(name)
                .domain(domain)
                .websiteUrl(websiteUrl)
                .logoUrl(logoUrl)
                .linkedinUrl(linkedinUrl)
                .twitterUrl(twitterUrl)
                .facebookUrl(facebookUrl)
                .phoneNumber(phoneNumber)
                .industry(industry)
                .revenueUsd(revenueUsd)
                .revenueUsdAmount(revenueUsdAmount)
                .employeeCount(employeeCount)
                .employeeRange(EmployeeRange.fromString(employeeCount))
                .hqCity(hqCity)
                .hqState(hqState)
                .hqCountry(hqCountry)
                .postalCode(postalCode)
                .territory(territory)
                .region(region)
                .icpTag(icpTag)
                .sicCodes(sicCodes)
                .naicsCodes(naicsCodes)
                .keywords(keywords)
                .technologies(technologies)
                .scrapedTechnologies(scrapedTechnologies)
                .scrapedTools(scrapedTools)
                .scrapedServices(scrapedServices)
                .publiclyTradedSymbol(publiclyTradedSymbol)
                .score(score)
                .accountSummary(accountSummary)
                .salespersonId(salespersonId)
                .salespersonName(salespersonName)
                .salespersonAssignAt(salespersonAssignAt)
                .source(source)
                .segments(segments)
                .tenantIds(tenantIds)
                .isTargetAccount(isTargetAccount)
                .leadCompanyStatus(leadCompanyStatus)
                .active(active)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

}
