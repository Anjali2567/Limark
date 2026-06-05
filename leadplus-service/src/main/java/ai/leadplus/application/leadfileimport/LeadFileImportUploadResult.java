package ai.leadplus.application.leadfileimport;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeadFileImportUploadResult {

    private LeadFileImportDto batch;
    private List<String> detectedColumns;
    private List<String> ignoredColumns;
    private String missingRequiredColumn;
    private String templateGuide;
    private Map<String, String> appliedMapping;
}