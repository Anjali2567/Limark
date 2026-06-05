package ai.leadplus.api.v1.leadcompanies;

import ai.leadplus.application.leadcompany.LeadCompanyDto;
import ai.leadplus.application.leadcompany.LeadCompanyWithContactCountDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LeadCompanySearchResponse extends LeadCompanyResponse {
    private long contactCount;

    public static LeadCompanySearchResponse toResponse(LeadCompanyDto dto, long contactCount) {
        return baseBuilder(LeadCompanySearchResponse.builder(), dto)
                .contactCount(contactCount)
                .build();
    }

    public static LeadCompanySearchResponse fromDto(LeadCompanyWithContactCountDto dto) {
        return baseBuilder(LeadCompanySearchResponse.builder(), dto)
                .contactCount(dto.getContactCount())
                .build();
    }
}