package ai.leadplus.api.v1.leads;

import ai.leadplus.application.leads.CompanyIdWithDomainDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyIdWithDomainResponse {
    private Long id;
    private String domain;

    public static CompanyIdWithDomainResponse fromDto(CompanyIdWithDomainDto dto) {
        if (dto == null) {
            return null;
        }
        return CompanyIdWithDomainResponse.builder()
                .id(dto.getId())
                .domain(dto.getDomain())
                .build();
    }
}
