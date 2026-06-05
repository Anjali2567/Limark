package ai.leadplus.domain.leadcompanyjobs;

import jakarta.persistence.Entity;
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
public class LeadCompanyJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long leadCompanyId;
    private String type;
    private String title;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "skills", columnDefinition = "varchar(255)[]")
    private List<String> skills;
    private String jobUrl;
    private String applyUrl;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "benefits", columnDefinition = "varchar(255)[]")
    private List<String> benefits;
    private String location;
    private String department;
    private LocalDateTime postedDate;
    private String description;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "requirements", columnDefinition = "varchar(255)[]")
    private List<String> requirements;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "technologies", columnDefinition = "varchar(255)[]")
    private List<String> technologies;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tools", columnDefinition = "varchar(255)[]")
    private List<String> tools;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "services", columnDefinition = "varchar(255)[]")
    private List<String> services;
    private boolean active;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
