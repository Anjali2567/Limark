package ai.leadplus.application.leadchatservice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadAudienceCount {
    private long totalCompanies;
    private long totalContacts;
    private List<String> sampleCompanyNames;
    private String feedback;
}
