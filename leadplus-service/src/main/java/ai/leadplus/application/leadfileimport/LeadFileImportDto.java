package ai.leadplus.application.leadfileimport;

import ai.leadplus.domain.leadfileimport.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadFileImportDto {

    private Long id;
    private List<ImportType> importTypes;
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
    private ImportStatus status;
    private String uploadedByUserId;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LeadFileImportDto fromEntity(LeadFileImport entity) {
        return LeadFileImportDto.builder()
                .id(entity.getId())
                .importTypes(entity.getImportTypes())
                .targetEntity(entity.getTargetEntity())
                .originalFileName(entity.getOriginalFileName())
                .fileStorageKey(entity.getFileStorageKey())
                .mappedFieldsJson(entity.getMappedFieldsJson())
                .totalRecords(entity.getTotalRecords())
                .processedRecords(entity.getProcessedRecords())
                .insertedRecords(entity.getInsertedRecords())
                .updatedRecords(entity.getUpdatedRecords())
                .skippedRecords(entity.getSkippedRecords())
                .failedRecords(entity.getFailedRecords())
                .status(entity.getStatus())
                .uploadedByUserId(entity.getUploadedByUserId())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}