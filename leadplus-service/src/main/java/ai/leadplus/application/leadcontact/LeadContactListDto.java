package ai.leadplus.application.leadcontact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadContactListDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String title;
    private String email;
    private String phoneE164;
    private String companyName;
}
