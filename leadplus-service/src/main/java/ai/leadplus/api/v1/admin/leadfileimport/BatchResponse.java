package ai.leadplus.api.v1.admin.leadfileimport;

import ai.leadplus.application.leadfileimport.LeadFileImportDto;
import ai.leadplus.domain.leadfileimport.ImportStatus;
import ai.leadplus.domain.leadfileimport.ImportType;
import ai.leadplus.domain.leadfileimport.TargetEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchResponse {
    private Long id;
    private List<ImportType> importTypes;
    private TargetEntity targetEntity;
    private String originalFileName;
    private ImportStatus status;
    private int totalRecords;
    private int insertedRecords;
    private int updatedRecords;
    private int skippedRecords;
    private int failedRecords;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    public static BatchResponse from(LeadFileImportDto dto) {
        return BatchResponse.builder()
                .id(dto.getId())
                .importTypes(dto.getImportTypes())
                .targetEntity(dto.getTargetEntity())
                .originalFileName(dto.getOriginalFileName())
                .status(dto.getStatus())
                .totalRecords(dto.getTotalRecords())
                .insertedRecords(dto.getInsertedRecords())
                .updatedRecords(dto.getUpdatedRecords())
                .skippedRecords(dto.getSkippedRecords())
                .failedRecords(dto.getFailedRecords())
                .startedAt(dto.getStartedAt())
                .completedAt(dto.getCompletedAt())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}