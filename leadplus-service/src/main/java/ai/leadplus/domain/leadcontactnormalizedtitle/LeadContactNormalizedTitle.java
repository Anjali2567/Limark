package ai.leadplus.domain.leadcontactnormalizedtitle;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import ai.leadplus.domain.common.TitleAbbreviationListConverter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
public class LeadContactNormalizedTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long leadContactId;
    private String originalTitle;
    private String canonicalTitle;
    private String seniority;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "normalized_titles", columnDefinition = "varchar(255)[]")
    private List<String> normalizedTitles;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "keywords", columnDefinition = "varchar(255)[]")
    private List<String> keywords;
    @Convert(converter = TitleAbbreviationListConverter.class)
    private List<TitleAbbreviation> titleAbbreviations;
    @CreatedDate
    private LocalDateTime createdAt;
}
