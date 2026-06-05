package ai.leadplus.application.apollocompanydata;

import ai.leadplus.domain.apollocompanydata.ApolloCompanyData;
import ai.leadplus.domain.common.ApolloDataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApolloCompanyDataDto {

    private Long id;
    private Long leadCompanyId;
    private ApolloDataType type;
    private String data;
    private Long specificationId;
    private LocalDateTime fetchedAt;

    public static ApolloCompanyDataDto fromEntity(ApolloCompanyData apolloCompanyData) {
        return ApolloCompanyDataDto.builder()
                .id(apolloCompanyData.getId())
                .leadCompanyId(apolloCompanyData.getLeadCompanyId())
                .type(apolloCompanyData.getType())
                .data(apolloCompanyData.getData())
                .specificationId(apolloCompanyData.getSpecificationId())
                .fetchedAt(apolloCompanyData.getFetchedAt())
                .build();
    }

    public ApolloCompanyData toEntity() {
        return ApolloCompanyData.builder()
                .id(id)
                .leadCompanyId(leadCompanyId)
                .type(type)
                .data(data)
                .specificationId(specificationId)
                .fetchedAt(fetchedAt)
                .build();
    }
}
