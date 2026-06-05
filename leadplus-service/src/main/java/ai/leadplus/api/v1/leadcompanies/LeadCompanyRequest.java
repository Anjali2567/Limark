package ai.leadplus.api.v1.leadcompanies;

import ai.leadplus.application.leadcompany.LeadCompanyDto;
import jakarta.validation.constraints.NotBlank;
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
public class LeadCompanyRequest {

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Domain is mandatory")
    private String domain;
    private String websiteUrl;
    private String linkedinUrl;
    private String twitterUrl;
    private String facebookUrl;
    private String industry;
    private BigDecimal revenueUsd;
    private String employeeCount;
    private String hqCity;
    private String hqState;
    private String hqCountry;
    private String postalCode;
    private String territory;
    private String icpTag;
    private List<String> sicCodes;
    private List<String> naicsCodes;
    private List<String> keywords;
    private List<String> technologies;
    private String publiclyTradedSymbol;
    private int score;
    private String accountSummary;
    private String source;
    private List<String> segments;
    private boolean isTargetAccount;

    public LeadCompanyDto toDto() {
        return LeadCompanyDto.builder()
                .name(name)
                .domain(domain)
                .websiteUrl(websiteUrl)
                .linkedinUrl(linkedinUrl)
                .twitterUrl(twitterUrl)
                .facebookUrl(facebookUrl)
                .industry(industry)
                .revenueUsd(revenueUsd)
                .revenueUsdAmount(revenueUsd != null ? revenueUsd.longValue() : null)
                .employeeCount(employeeCount)
                .hqCity(hqCity)
                .hqState(hqState)
                .hqCountry(hqCountry)
                .postalCode(postalCode)
                .territory(territory)
                .icpTag(icpTag)
                .sicCodes(sicCodes)
                .naicsCodes(naicsCodes)
                .keywords(keywords)
                .technologies(technologies)
                .publiclyTradedSymbol(publiclyTradedSymbol)
                .score(score)
                .accountSummary(accountSummary)
                .source(source)
                .segments(segments)
                .isTargetAccount(isTargetAccount)
                .build();
    }
}
