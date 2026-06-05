package ai.leadplus.domain.leadcontacts;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;
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

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
public class LeadContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String apolloId;
    private Long leadCompanyId;
    private String firstName;
    private String lastName;
    private String firstNameNormalized;
    private String lastNameNormalized;
    private String fullName;
    private String title;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "normalized_title_tokens", columnDefinition = "varchar(255)[]")
    private List<String> normalizedTitleTokens;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tenant_ids", columnDefinition = "varchar(255)[]")
    private List<String> tenantIds;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "segments", columnDefinition = "varchar(255)[]")
    private List<String> segments;
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
    private String personaMatch;
    private Integer personaScore;
    private Long ownerId;
    private String consentStatus;
    private boolean doNotContact;
    private String source;
    private String notes;
    private boolean apolloEnriched;
    private boolean active;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    @Enumerated(EnumType.STRING)
    private DataSource dataSource;
}
