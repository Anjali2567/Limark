package ai.leadplus.application.leadfileimport;

import ai.leadplus.domain.leadfileimport.ImportType;
import ai.leadplus.domain.leadfileimport.LeadFileImportRecord;
import ai.leadplus.domain.leadfileimport.RecordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadFileImportRecordDto {

    private Long id;
    private Long importId;
    private int recordNumber;
    private String rawPayloadJson;
    private String normalizedPayloadJson;
    private String matchKey;
    private ImportType resolvedImportType;
    private RecordStatus status;
    private Long targetEntityId;
    private String errorCode;
    private String errorMessage;
    private String changesJson;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;

    public static LeadFileImportRecordDto fromEntity(LeadFileImportRecord entity) {
        return LeadFileImportRecordDto.builder()
                .id(entity.getId())
                .importId(entity.getImportId())
                .recordNumber(entity.getRecordNumber())
                .rawPayloadJson(entity.getRawPayloadJson())
                .normalizedPayloadJson(entity.getNormalizedPayloadJson())
                .matchKey(entity.getMatchKey())
                .resolvedImportType(entity.getResolvedImportType())
                .status(entity.getStatus())
                .targetEntityId(entity.getTargetEntityId())
                .errorCode(entity.getErrorCode())
                .errorMessage(entity.getErrorMessage())
                .changesJson(entity.getChangesJson())
                .processedAt(entity.getProcessedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}