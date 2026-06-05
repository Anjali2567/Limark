package ai.leadplus.domain.leadcontacts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyContactCount {
    private Long companyId;
    private long count;
}
