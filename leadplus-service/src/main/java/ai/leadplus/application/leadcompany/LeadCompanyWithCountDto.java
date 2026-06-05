package ai.leadplus.application.leadcompany;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Page;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LeadCompanyWithCountDto {
    private Page<LeadCompanyWithContactCountDto> companies;
    private Integer companiesCount;
    private Integer contactsCount;
}
