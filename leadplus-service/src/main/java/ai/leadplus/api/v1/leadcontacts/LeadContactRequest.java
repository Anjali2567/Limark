package ai.leadplus.api.v1.leadcontacts;

import ai.leadplus.application.leadcontact.LeadContactDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadContactRequest {
    private String firstName;
    private String lastName;
    private String title;
    private String seniority;
    private String department;
    private String email;
    private String emailStatus;
    private String phoneE164;
    private String linkedinUrl;
    private String locationCity;
    private String locationState;
    private String locationCountry;
    private String locationZip;
    private String personaMatch;
    private Integer personaScore;
    private Long ownerId;
    private String consentStatus;
    private boolean doNotContact;
    private String source;
    private String notes;

    public LeadContactDto toDto() {
        return LeadContactDto.builder()
                .firstName(firstName)
                .lastName(lastName)
                .title(title)
                .seniority(seniority)
                .department(department)
                .email(email)
                .emailStatus(emailStatus)
                .phoneE164(phoneE164)
                .linkedinUrl(linkedinUrl)
                .locationCity(locationCity)
                .locationState(locationState)
                .locationCountry(locationCountry)
                .locationZip(locationZip)
                .personaMatch(personaMatch)
                .personaScore(personaScore)
                .ownerId(ownerId)
                .consentStatus(consentStatus)
                .doNotContact(doNotContact)
                .source(source)
                .notes(notes)
                .build();
    }
}
