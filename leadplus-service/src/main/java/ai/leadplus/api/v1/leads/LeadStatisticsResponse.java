package ai.leadplus.api.v1.leads;

import ai.leadplus.application.leads.LeadStatisticsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadStatisticsResponse {

    private long totalContacts;
    private long totalCompanies;
    private long totalEmails;
    private long totalPhoneNumbers;

    public static LeadStatisticsResponse fromDto(LeadStatisticsDto leadStatisticsDto) {
        return LeadStatisticsResponse.builder()
                .totalContacts(leadStatisticsDto.getTotalContacts())
                .totalCompanies(leadStatisticsDto.getTotalCompanies())
                .totalEmails(leadStatisticsDto.getTotalEmails())
                .totalPhoneNumbers(leadStatisticsDto.getTotalPhoneNumbers())
                .build();
    }
}
