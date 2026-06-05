package ai.leadplus.application.campaignagent.tools;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentLeadCountResult {
    private long totalCompanies;
    private long totalContacts;
    private List<String> sampleCompanyNames;
}