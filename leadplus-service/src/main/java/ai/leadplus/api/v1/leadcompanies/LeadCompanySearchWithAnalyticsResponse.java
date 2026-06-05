package ai.leadplus.api.v1.leadcompanies;

import ai.leadplus.application.leadcompany.LeadCompanyWithCountDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Page;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LeadCompanySearchWithAnalyticsResponse {
    private Page<LeadCompanySearchResponse> companies;
    private Integer totalCompanies;
    private Integer totalContacts;

    public static LeadCompanySearchWithAnalyticsResponse fromDto(LeadCompanyWithCountDto dto) {
        return LeadCompanySearchWithAnalyticsResponse.builder()
                .companies(dto.getCompanies().map(LeadCompanySearchResponse::fromDto))
                .totalCompanies(dto.getCompaniesCount())
                .totalContacts(dto.getContactsCount())
                .build();
    }
}
