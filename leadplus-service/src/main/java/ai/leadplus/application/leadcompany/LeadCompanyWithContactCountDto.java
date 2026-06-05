package ai.leadplus.application.leadcompany;

import ai.leadplus.domain.leadcompanies.LeadCompany;
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
public class LeadCompanyWithContactCountDto extends LeadCompanyDto {
    private long contactCount;

    public static LeadCompanyWithContactCountDto fromEntity(LeadCompany leadCompany, long contactCount) {
        return baseBuilder(LeadCompanyWithContactCountDto.builder(), leadCompany)
                .contactCount(contactCount)
                .build();
    }
}
