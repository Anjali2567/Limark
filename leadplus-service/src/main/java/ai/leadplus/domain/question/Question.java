package ai.leadplus.domain.question;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "question_section_id")
    private Long questionSectionId;
    private Integer position;
    @Enumerated(EnumType.STRING)
    private QuestionType type;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "industry_ids", columnDefinition = "bigint[]")
    private List<Long> industryIds;
    private String label;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "options", columnDefinition = "varchar(255)[]")
    private List<String> options;
    private boolean active;
    @CreatedDate
    private LocalDateTime createdAt;
    @CreatedBy
    private Long createdBy;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    @LastModifiedBy
    private Long updatedBy;
}
