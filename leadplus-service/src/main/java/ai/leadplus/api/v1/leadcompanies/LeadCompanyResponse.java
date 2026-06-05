package ai.leadplus.api.v1.leadcompanies;

import ai.leadplus.application.leadcompany.LeadCompanyDto;
import ai.leadplus.domain.leadcompanies.LeadCompanyStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LeadCompanyResponse {

    private Long id;
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
    private List<String> tools;
    private List<String> services;
    private String publiclyTradedSymbol;
    private int score;
    private String accountSummary;
    private Long salespersonId;
    private String salespersonName;
    private String source;
    private List<String> segments;
    private boolean isTargetAccount;
    private LeadCompanyStatus leadCompanyStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LeadCompanyResponse toResponse(LeadCompanyDto dto) {
        if (dto == null) {
            return null;
        }
        return baseBuilder(LeadCompanyResponse.builder(), dto)
                .build();
    }

    @SuppressWarnings("unchecked")
    protected static <T extends LeadCompanyResponseBuilder<?, ?>> T baseBuilder(T builder, LeadCompanyDto dto) {
        if (dto == null) {
            return null;
        }
        return (T) builder
                .id(dto.getId())
                .name(dto.getName())
                .domain(dto.getDomain())
                .websiteUrl(dto.getWebsiteUrl())
                .logoUrl(dto.getLogoUrl())
                .linkedinUrl(dto.getLinkedinUrl())
                .twitterUrl(dto.getTwitterUrl())
                .facebookUrl(dto.getFacebookUrl())
                .phoneNumber(dto.getPhoneNumber())
                .industry(dto.getIndustry())
                .revenueUsd(dto.getRevenueUsd())
                .employeeCount(dto.getEmployeeCount())
                .hqCity(dto.getHqCity())
                .hqState(dto.getHqState())
                .hqCountry(dto.getHqCountry())
                .postalCode(dto.getPostalCode())
                .territory(dto.getTerritory())
                .icpTag(dto.getIcpTag())
                .sicCodes(dto.getSicCodes())
                .naicsCodes(dto.getNaicsCodes())
                .keywords(dto.getKeywords())
                .technologies(Stream.of(dto.getTechnologies(), dto.getScrapedTechnologies())
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .distinct()
                        .collect(Collectors.toList()))
                .tools(dto.getScrapedTools())
                .services(dto.getScrapedServices())
                .publiclyTradedSymbol(dto.getPubliclyTradedSymbol())
                .score(dto.getScore())
                .accountSummary(dto.getAccountSummary())
                .salespersonId(dto.getSalespersonId())
                .salespersonName(dto.getSalespersonName())
                .source(dto.getSource())
                .segments(dto.getSegments())
                .isTargetAccount(dto.isTargetAccount())
                .leadCompanyStatus(dto.getLeadCompanyStatus())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt());
    }
}
