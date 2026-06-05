package ai.leadplus.api.v1.leadcontacts;

import ai.leadplus.application.leadcontact.LeadContactListDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadContactListResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String title;
    private String email;
    private String phoneE164;
    private String companyName;

    public static LeadContactListResponse fromDto(LeadContactListDto leadContactListDto) {
        if (leadContactListDto == null) return null;
        return LeadContactListResponse.builder()
                .id(leadContactListDto.getId())
                .firstName(leadContactListDto.getFirstName())
                .lastName(leadContactListDto.getLastName())
                .title(leadContactListDto.getTitle())
                .email(leadContactListDto.getEmail())
                .phoneE164(leadContactListDto.getPhoneE164())
                .companyName(leadContactListDto.getCompanyName())
                .build();
    }
}
