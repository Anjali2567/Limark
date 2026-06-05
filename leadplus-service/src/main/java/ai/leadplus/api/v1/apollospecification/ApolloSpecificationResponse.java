package ai.leadplus.api.v1.apollospecification;

import ai.leadplus.application.apollospecification.ApolloSpecificationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApolloSpecificationResponse {

    private Long id;
    private boolean personTitleEnabled;
    private List<String> personTitles;
    private boolean personSeniorityEnabled;
    private List<String> personSeniorities;
    private LocalDateTime createdAt;
    private Long createdBy;

    public static ApolloSpecificationResponse fromDto(ApolloSpecificationDto dto) {
        if (dto == null) {
            return null;
        }
        return ApolloSpecificationResponse.builder()
                .id(dto.getId())
                .personTitleEnabled(dto.isPersonTitleEnabled())
                .personTitles(dto.getPersonTitles())
                .personSeniorityEnabled(dto.isPersonSeniorityEnabled())
                .personSeniorities(dto.getPersonSeniorities())
                .createdAt(dto.getCreatedAt())
                .createdBy(dto.getCreatedBy())
                .build();
    }
}
