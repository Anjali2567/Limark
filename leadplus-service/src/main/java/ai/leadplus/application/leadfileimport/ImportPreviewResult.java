package ai.leadplus.application.leadfileimport;

import ai.leadplus.domain.leadfileimport.ImportType;
import ai.leadplus.domain.leadfileimport.RecordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportPreviewResult {

    private int totalRows;
    private int insertCount;
    private int updateCount;
    private int skipCount;

    private List<InsertPreviewItem>  toInsert;
    private List<UpdatePreviewItem>  toUpdate;
    private List<SkipPreviewItem>    toSkip;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InsertPreviewItem {
        private int rowNumber;
        private String matchKey;
        private ImportType resolvedImportType;
        private Map<String, String> fieldsToCreate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdatePreviewItem {
        private int rowNumber;
        private String matchKey;
        private Long existingEntityId;
        private ImportType resolvedImportType;
        private List<String> fieldsToFill;
        private Map<String, String> newValues;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SkipPreviewItem {
        private int rowNumber;
        private String matchKey;
        private RecordStatus reason;
        private String errorCode;
        private String errorMessage;
        private Map<String, String> normalizedPayload;
    }
}