package ai.leadplus.domain.requestforquotes;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import ai.leadplus.domain.common.BudgetRange;
import ai.leadplus.domain.common.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
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
public class RequestForQuote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String title;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "service_ids", columnDefinition = "varchar(255)[]")
    private List<Long> serviceIds;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "vendor_ids", columnDefinition = "varchar(255)[]")
    private List<Long> vendorIds;
    private int quantity;
    @Enumerated(EnumType.STRING)
    private BudgetRange budget;
    private LocalDateTime deadline;
    private String description;
    @Enumerated(EnumType.STRING)
    private RequestStatus status;
    private boolean active;
    @CreatedBy
    private Long createdBy;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedBy
    private Long updatedBy;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
