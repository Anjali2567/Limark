package ai.leadplus.api.v1.admin.leadfileimport;

import ai.leadplus.application.leadfileimport.LeadFileImportUploadResult;
import ai.leadplus.domain.leadfileimport.ImportStatus;
import ai.leadplus.domain.leadfileimport.TargetEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadResponse {
    private Long batchId;
    private TargetEntity targetEntity;
    private ImportStatus status;
    private String originalFileName;
    private LocalDateTime createdAt;
    private List<String> detectedColumns;
    private List<String> ignoredColumns;
    private String missingRequiredColumn;
    private String templateGuide;
    private Map<String, String> appliedMapping;

    public static UploadResponse from(LeadFileImportUploadResult r) {
        return UploadResponse.builder()
                .batchId(r.getBatch().getId())
                .targetEntity(r.getBatch().getTargetEntity())
                .status(r.getBatch().getStatus())
                .originalFileName(r.getBatch().getOriginalFileName())
                .createdAt(r.getBatch().getCreatedAt())
                .detectedColumns(r.getDetectedColumns())
                .ignoredColumns(r.getIgnoredColumns())
                .missingRequiredColumn(r.getMissingRequiredColumn())
                .templateGuide(r.getTemplateGuide())
                .appliedMapping(r.getAppliedMapping())
                .build();
    }
}