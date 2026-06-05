package ai.leadplus.domain.leadsearchhistories;

import jakarta.persistence.*;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import ai.leadplus.domain.common.EmployeeRangeListConverter;
import ai.leadplus.domain.common.LeadType;
import ai.leadplus.domain.leadcompanies.EmployeeRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "lead_search_history")
public class LeadSearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String title;
    private Integer resultCount;
    @Enumerated(EnumType.STRING)
    private LeadType type;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "cities", columnDefinition = "varchar(255)[]")
    private List<String> cities;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "states", columnDefinition = "varchar(255)[]")
    private List<String> states;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "countries", columnDefinition = "varchar(255)[]")
    private List<String> countries;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "contact_names", columnDefinition = "varchar(255)[]")
    private List<String> contactNames;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "company_names", columnDefinition = "varchar(255)[]")
    private List<String> companyNames;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "company_cities", columnDefinition = "varchar(255)[]")
    private List<String> companyCities;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "company_states", columnDefinition = "varchar(255)[]")
    private List<String> companyStates;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "company_countries", columnDefinition = "varchar(255)[]")
    private List<String> companyCountries;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "regions", columnDefinition = "varchar(255)[]")
    private List<String> regions;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "keywords", columnDefinition = "varchar(255)[]")
    private List<String> keywords;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "industries", columnDefinition = "varchar(255)[]")
    private List<String> industries;
    @Convert(converter = EmployeeRangeListConverter.class)
    @Column(name = "employee_ranges", columnDefinition = "varchar(255)[]")
    private List<EmployeeRange> employeeRanges;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "revenue_ranges", columnDefinition = "varchar(255)[]")
    private List<String> revenueRanges;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "technologies", columnDefinition = "varchar(255)[]")
    private List<String> technologies;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "titles", columnDefinition = "varchar(255)[]")
    private List<String> titles;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "seniority", columnDefinition = "varchar(255)[]")
    private List<String> seniority;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "departments", columnDefinition = "varchar(255)[]")
    private List<String> departments;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "postal_codes", columnDefinition = "varchar(255)[]")
    private List<String> postalCodes;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "sic_codes", columnDefinition = "varchar(255)[]")
    private List<String> sicCodes;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "naics_codes", columnDefinition = "varchar(255)[]")
    private List<String> naicsCodes;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
