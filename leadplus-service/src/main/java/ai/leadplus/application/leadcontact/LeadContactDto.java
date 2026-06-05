package ai.leadplus.application.leadcontact;

import ai.leadplus.domain.leadcontacts.DataSource;
import ai.leadplus.domain.leadcontacts.LeadContact;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadContactDto {
    private Long id;
    private String apolloId;
    private Long leadCompanyId;
    private String firstName;
    private String lastName;
    private String firstNameNormalized;
    private String lastNameNormalized;
    private String fullName;
    private String title;
    private List<String> normalizedTitleTokens;
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
    private boolean apolloEnriched;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private DataSource dataSource;

    public static LeadContactDto toDto(LeadContact leadContact) {
        if (leadContact == null) {
            return null;
        }
        return LeadContactDto.builder()
                .id(leadContact.getId())
                .apolloId(leadContact.getApolloId())
                .leadCompanyId(leadContact.getLeadCompanyId())
                .firstName(leadContact.getFirstName())
                .lastName(leadContact.getLastName())
                .firstNameNormalized(leadContact.getFirstNameNormalized())
                .lastNameNormalized(leadContact.getLastNameNormalized())
                .fullName(leadContact.getFullName())
                .title(leadContact.getTitle())
                .normalizedTitleTokens(leadContact.getNormalizedTitleTokens())
                .seniority(leadContact.getSeniority())
                .department(leadContact.getDepartment())
                .email(leadContact.getEmail())
                .emailStatus(leadContact.getEmailStatus())
                .phoneE164(leadContact.getPhoneE164())
                .linkedinUrl(leadContact.getLinkedinUrl())
                .locationCity(leadContact.getLocationCity())
                .locationState(leadContact.getLocationState())
                .locationCountry(leadContact.getLocationCountry())
                .locationZip(leadContact.getLocationZip())
                .personaMatch(leadContact.getPersonaMatch())
                .personaScore(leadContact.getPersonaScore())
                .ownerId(leadContact.getOwnerId())
                .consentStatus(leadContact.getConsentStatus())
                .doNotContact(leadContact.isDoNotContact())
                .source(leadContact.getSource())
                .notes(leadContact.getNotes())
                .apolloEnriched(leadContact.isApolloEnriched())
                .active(leadContact.isActive())
                .createdAt(leadContact.getCreatedAt())
                .updatedAt(leadContact.getUpdatedAt())
                .dataSource(leadContact.getDataSource())
                .build();
    }

    public LeadContact toEntity() {
        return LeadContact.builder()
                .id(id)
                .apolloId(apolloId)
                .leadCompanyId(leadCompanyId)
                .firstName(firstName)
                .lastName(lastName)
                .firstNameNormalized(firstNameNormalized)
                .lastNameNormalized(lastNameNormalized)
                .fullName(fullName)
                .title(title)
                .normalizedTitleTokens(normalizedTitleTokens)
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
                .apolloEnriched(apolloEnriched)
                .active(active)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .dataSource(dataSource)
                .build();
    }

}
