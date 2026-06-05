package ai.leadplus.api.v1.apollospecification;

import ai.leadplus.application.apollospecification.ApolloSpecificationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApolloSpecificationRequest {

    private boolean personTitleEnabled;
    private List<String> personTitles;
    private boolean personSeniorityEnabled;
    private List<String> personSeniorities;

    public ApolloSpecificationDto toDto() {
        return ApolloSpecificationDto.builder()
                .personTitleEnabled(personTitleEnabled)
                .personTitles(personTitles)
                .personSeniorityEnabled(personSeniorityEnabled)
                .personSeniorities(personSeniorities)
                .build();
    }
}
