package ai.leadplus.domain.leadfileimport;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ai.leadplus.domain.common.ImportTypeListConverter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
public class LeadFileImport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Convert(converter = ImportTypeListConverter.class)
    @Column(name = "import_types", columnDefinition = "text")
    private List<ImportType> importTypes;
    @Enumerated(EnumType.STRING)
    private TargetEntity targetEntity;
    private String originalFileName;
    private String fileStorageKey;
    private String mappedFieldsJson;
    private int totalRecords;
    private int processedRecords;
    private int insertedRecords;
    private int updatedRecords;
    private int skippedRecords;
    private int failedRecords;
    @Enumerated(EnumType.STRING)
    private ImportStatus status;
    private String uploadedByUserId;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
