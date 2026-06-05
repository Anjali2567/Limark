package ai.leadplus.api.v1.admin.leadfileimport;

import ai.leadplus.application.leadfileimport.ImportPreviewResult;
import ai.leadplus.domain.leadfileimport.ImportType;
import ai.leadplus.domain.leadfileimport.RecordStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewResponse {
    private int totalRows;
    private int insertCount;
    private int updateCount;
    private int skipCount;
    private List<InsertItem> toInsert;
    private List<UpdateItem> toUpdate;
    private List<SkipItem> toSkip;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InsertItem {
        private int rowNumber;
        private String matchKey;
        private ImportType resolvedImportType;
        private Map<String, String> fieldsToCreate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateItem {
        private int rowNumber;
        private String matchKey;
        private Long existingEntityId;
        private ImportType resolvedImportType;
        private List<String> fieldsToFill;
        private Map<String, String> newValues;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SkipItem {
        private int rowNumber;
        private String matchKey;
        private RecordStatus reason;
        private String errorCode;
        private String errorMessage;
        private Map<String, String> normalizedPayload;
    }

    public static PreviewResponse from(ImportPreviewResult r) {
        return PreviewResponse.builder()
                .totalRows(r.getTotalRows())
                .insertCount(r.getInsertCount())
                .updateCount(r.getUpdateCount())
                .skipCount(r.getSkipCount())
                .toInsert(r.getToInsert().stream().map(i ->
                        InsertItem.builder()
                                .rowNumber(i.getRowNumber())
                                .matchKey(i.getMatchKey())
                                .resolvedImportType(i.getResolvedImportType())
                                .fieldsToCreate(i.getFieldsToCreate())
                                .build()).toList())
                .toUpdate(r.getToUpdate().stream().map(i ->
                        UpdateItem.builder()
                                .rowNumber(i.getRowNumber())
                                .matchKey(i.getMatchKey())
                                .existingEntityId(i.getExistingEntityId())
                                .resolvedImportType(i.getResolvedImportType())
                                .fieldsToFill(i.getFieldsToFill())
                                .newValues(i.getNewValues())
                                .build()).toList())
                .toSkip(r.getToSkip().stream().map(i ->
                        SkipItem.builder()
                                .rowNumber(i.getRowNumber())
                                .matchKey(i.getMatchKey())
                                .reason(i.getReason())
                                .errorCode(i.getErrorCode())
                                .errorMessage(i.getErrorMessage())
                                .normalizedPayload(i.getNormalizedPayload())
                                .build()).toList())
                .build();
    }
}
