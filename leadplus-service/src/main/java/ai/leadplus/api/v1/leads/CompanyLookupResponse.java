package ai.leadplus.api.v1.leads;

import ai.leadplus.application.leads.CompanyLookupDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyLookupResponse {
    private Long id;
    private String name;
    private String domain;

    public static CompanyLookupResponse fromDto(CompanyLookupDto dto) {
        if (dto == null) {
            return null;
        }
        return CompanyLookupResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .domain(dto.getDomain())
                .build();
    }
}
