package ai.leadplus.domain.leadfileimport;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
public class LeadFileImportRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long importId;
    private int recordNumber;
    private String rawPayloadJson;
    private String normalizedPayloadJson;
    private String matchKey;
    @Enumerated(EnumType.STRING)
    private ImportType resolvedImportType;
    @Enumerated(EnumType.STRING)
    private RecordStatus status;
    private Long targetEntityId;
    private String errorCode;
    private String errorMessage;
    private String changesJson;
    private LocalDateTime processedAt;
    @CreatedDate
    private LocalDateTime createdAt;
}