package ai.leadplus.application.apollospecification;

import ai.leadplus.domain.apollospecification.ApolloSpecification;
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
public class ApolloSpecificationDto {

    private Long id;
    private boolean personTitleEnabled;
    private List<String> personTitles;
    private boolean personSeniorityEnabled;
    private List<String> personSeniorities;
    private LocalDateTime createdAt;
    private Long createdBy;

    public static ApolloSpecificationDto fromSpecification(ApolloSpecification apolloSpecification) {
        if (apolloSpecification == null) {
            return null;
        }
        return ApolloSpecificationDto.builder()
                .id(apolloSpecification.getId())
                .personTitleEnabled(apolloSpecification.isPersonTitleEnabled())
                .personTitles(apolloSpecification.getPersonTitles())
                .personSeniorityEnabled(apolloSpecification.isPersonSeniorityEnabled())
                .personSeniorities(apolloSpecification.getPersonSeniorities())
                .createdAt(apolloSpecification.getCreatedAt())
                .createdBy(apolloSpecification.getCreatedBy())
                .build();
    }

    public ApolloSpecification toSpecification() {
        return ApolloSpecification.builder()
                .id(id)
                .personTitleEnabled(personTitleEnabled)
                .personTitles(personTitles)
                .personSeniorityEnabled(personSeniorityEnabled)
                .personSeniorities(personSeniorities)
                .build();
    }
}
