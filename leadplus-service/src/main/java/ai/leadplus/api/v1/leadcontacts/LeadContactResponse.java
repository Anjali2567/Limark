package ai.leadplus.api.v1.leadcontacts;

import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.domain.leadcontacts.DataSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadContactResponse {
    private Long id;
    private Long leadCompanyId;
    private String firstName;
    private String lastName;
    private String firstNameNormalized;
    private String lastNameNormalized;
    private String fullName;
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
    private DataSource dataSource;
    private String source;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LeadContactResponse toResponse(LeadContactDto dto) {
        if (dto == null) {
            return null;
        }
        return LeadContactResponse.builder()
                .id(dto.getId())
                .leadCompanyId(dto.getLeadCompanyId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .firstNameNormalized(dto.getFirstNameNormalized())
                .lastNameNormalized(dto.getLastNameNormalized())
                .fullName(dto.getFullName())
                .title(dto.getTitle())
                .seniority(dto.getSeniority())
                .department(dto.getDepartment())
                .email(dto.getEmail())
                .emailStatus(dto.getEmailStatus())
                .phoneE164(dto.getPhoneE164())
                .linkedinUrl(dto.getLinkedinUrl())
                .locationCity(dto.getLocationCity())
                .locationState(dto.getLocationState())
                .locationCountry(dto.getLocationCountry())
                .locationZip(dto.getLocationZip())
                .personaMatch(dto.getPersonaMatch())
                .personaScore(dto.getPersonaScore())
                .ownerId(dto.getOwnerId())
                .consentStatus(dto.getConsentStatus())
                .doNotContact(dto.isDoNotContact())
                .source(dto.getSource())
                .notes(dto.getNotes())
                .dataSource(dto.getDataSource())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }


}
