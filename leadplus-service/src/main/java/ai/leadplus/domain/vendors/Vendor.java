package ai.leadplus.domain.vendors;

import jakarta.persistence.Entity;
import jakarta.persistence.Convert;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ai.leadplus.domain.common.AddressConverter;
import ai.leadplus.domain.common.AnswerListConverter;
import ai.leadplus.domain.common.SocialMediaConverter;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long tenantId;
    private String companyName;
    private String description;
    private String logo;
    private String companySize;
    private String phoneNumber;
    private String faxNumber;
    private String salesEmail;
    private String schedulingLink;
    private String website;
    private String videoLink;
    private String annualRevenue;
    private String tagline;
    private String businessHours;
    private String yearEstablished;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "languages_spoken", columnDefinition = "varchar(255)[]")
    private List<String> languagesSpoken;
    private String teamDescription;
    private String teamPhoto;
    @Convert(converter = AddressConverter.class)
    private Address address;
    @Convert(converter = SocialMediaConverter.class)
    private SocialMedia socialMedia;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "industry_ids", columnDefinition = "varchar(255)[]")
    private List<Long> industryIds;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "service_ids", columnDefinition = "varchar(255)[]")
    private List<Long> serviceIds;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "specification_ids", columnDefinition = "varchar(255)[]")
    private List<Long> specificationIds;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "regions_covered", columnDefinition = "varchar(255)[]")
    private List<String> regionsCovered;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "certifications", columnDefinition = "varchar(255)[]")
    private List<String> certifications;
    private String minProjectSize;
    private String avgHourlyRate;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "client_budgets", columnDefinition = "varchar(100)[]")
    private List<String> clientBudgets;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "client_sizes", columnDefinition = "varchar(100)[]")
    private List<String> clientSizes;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "client_employee_ranges", columnDefinition = "varchar(100)[]")
    private List<String> clientEmployeeRanges;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "client_locations", columnDefinition = "varchar(255)[]")
    private List<String> clientLocations;
    private boolean openToAnyLocation;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "client_industries", columnDefinition = "varchar(255)[]")
    private List<String> clientIndustries;
    private boolean openToAnyIndustry;
    @Enumerated(EnumType.STRING)
    private VendorVerificationStatus vendorVerificationStatus;
    @Convert(converter = AnswerListConverter.class)
    private List<Answer> questionnaire;
    private String reviewComment;
    private boolean active;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
