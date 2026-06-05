package ai.leadplus.application.campaigngenerator.tools;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadSearchToolResponse {
    private long totalCompanies;
    private long totalContacts;
    private String searchResultId;
    private String message;
    private List<String> sampleCompanyNames;
}