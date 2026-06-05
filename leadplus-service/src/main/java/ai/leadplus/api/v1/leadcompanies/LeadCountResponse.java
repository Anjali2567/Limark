package ai.leadplus.api.v1.leadcompanies;

import ai.leadplus.application.leadcontact.LeadCountDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadCountResponse {

    private long totalCompanies;
    private long totalContacts;

    public static LeadCountResponse fromDto(LeadCountDto leadCountDto) {
        return LeadCountResponse.builder()
                .totalCompanies(leadCountDto.getTotalCompanies())
                .totalContacts(leadCountDto.getTotalContacts())
                .build();
    }
}
