package ai.leadplus.application.leads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadStatisticsDto {
    private long totalContacts;
    private long totalCompanies;
    private long totalEmails;
    private long totalPhoneNumbers;
}
