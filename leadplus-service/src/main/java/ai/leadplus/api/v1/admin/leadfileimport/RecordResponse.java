package ai.leadplus.api.v1.admin.leadfileimport;

import ai.leadplus.application.leadfileimport.LeadFileImportRecordDto;
import ai.leadplus.domain.leadfileimport.ImportType;
import ai.leadplus.domain.leadfileimport.RecordStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecordResponse {
    private Long id;
    private Long importId;
    private int recordNumber;
    private String matchKey;
    private ImportType resolvedImportType;
    private RecordStatus status;
    private Long targetEntityId;
    private String errorCode;
    private String errorMessage;
    private String changesJson;
    private LocalDateTime processedAt;

    public static RecordResponse from(LeadFileImportRecordDto dto) {
        return RecordResponse.builder()
                .id(dto.getId())
                .importId(dto.getImportId())
                .recordNumber(dto.getRecordNumber())
                .matchKey(dto.getMatchKey())
                .resolvedImportType(dto.getResolvedImportType())
                .status(dto.getStatus())
                .targetEntityId(dto.getTargetEntityId())
                .errorCode(dto.getErrorCode())
                .errorMessage(dto.getErrorMessage())
                .changesJson(dto.getChangesJson())
                .processedAt(dto.getProcessedAt())
                .build();
    }
}
