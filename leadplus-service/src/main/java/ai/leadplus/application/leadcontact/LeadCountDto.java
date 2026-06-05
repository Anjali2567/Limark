package ai.leadplus.application.leadcontact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadCountDto {
    private long totalCompanies;
    private long totalContacts;
}
