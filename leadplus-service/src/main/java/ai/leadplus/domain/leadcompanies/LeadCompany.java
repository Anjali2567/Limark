package ai.leadplus.domain.leadcompanies;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "lead_company")
public class LeadCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Enumerated(EnumType.STRING)
    private EmployeeRange employeeRange;
    private String hqCity;
    private String hqState;
    private String hqCountry;
    private String postalCode;
    private String territory;
    private String region;
    private String icpTag;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "sic_codes", columnDefinition = "varchar(255)[]")
    private List<String> sicCodes;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "naics_codes", columnDefinition = "varchar(255)[]")
    private List<String> naicsCodes;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "keywords", columnDefinition = "varchar(255)[]")
    private List<String> keywords;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "technologies", columnDefinition = "varchar(255)[]")
    private List<String> technologies;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "scraped_technologies", columnDefinition = "varchar(255)[]")
    private List<String> scrapedTechnologies;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "scraped_tools", columnDefinition = "varchar(255)[]")
    private List<String> scrapedTools;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "scraped_services", columnDefinition = "varchar(255)[]")
    private List<String> scrapedServices;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "segments", columnDefinition = "varchar(255)[]")
    private List<String> segments;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tenant_ids", columnDefinition = "varchar(255)[]")
    private List<String> tenantIds;
    private String publiclyTradedSymbol;
    private int score;
    private String accountSummary;
    private Long salespersonId;
    private String salespersonName;
    private LocalDateTime salespersonAssignAt;
    private String source;
    private boolean isTargetAccount;
    @Enumerated(EnumType.STRING)
    private LeadCompanyStatus leadCompanyStatus;
    private boolean active;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
