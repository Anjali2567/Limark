package ai.leadplus.domain.quotations;

import jakarta.persistence.Entity;
import jakarta.persistence.Convert;
import ai.leadplus.domain.common.QuotationItemListConverter;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import ai.leadplus.domain.common.BudgetRange;
import ai.leadplus.domain.common.SourcingRequestType;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Quotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long sourceId;
    private Long tenantId;
    private Long workspaceId;
    private String title;
    private String description;
    @Enumerated(EnumType.STRING)
    private BudgetRange budget;
    private LocalDateTime deadline;
    @Convert(converter = QuotationItemListConverter.class)
    private List<QuotationItem> items;
    private String paymentTerms;
    private List<String> deliverables;
    @Enumerated(EnumType.STRING)
    private QuotationStatus status;
    @Enumerated(EnumType.STRING)
    private SourcingRequestType sourceType;
    private Long vendorId;
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
